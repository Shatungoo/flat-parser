package com.helldaisy


import com.helldaisy.Db.FlatTable
import com.helldaisy.ui.Filter
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.jackson.json
import org.ktorm.schema.*
import org.ktorm.support.sqlite.insertOrUpdate
import org.ktorm.support.sqlite.jsonExtract
import java.nio.file.Paths
import java.sql.SQLException


fun main() {
    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
    val db = Db(path)

    val query = db.connection.from(FlatTable).select(FlatTable.flat)
        .where(FlatTable.area.eq(3))
            .whereWithConditions {
                it += FlatTable.rooms.greaterEq(2)
//                it += FlatTable.price.lt(200000)
            }
        .limit(2)

        println(query.sql)
        query.forEach {
            println(it[FlatTable.flat]!!.room)
        }

}

fun String.toFlat(): Response.Flat = json.decodeFromString(Response.Flat.serializer(), this)

class Db(path: String = "./flats") {
    val connection = Database.connect("jdbc:sqlite:$path.db")

    object FlatTable : Table<Nothing>("flats") {
        val id = int("id").primaryKey()
        val flat = json<Response.Flat>("flat")

        val lastUpdated get() = this.flat.jsonExtract("$.last_updated", LocalDateTimeSqlType)

        val price get() = this.flat.jsonExtract("$.price.2.price_total", IntSqlType)
        val priceSquare get() = this.flat.jsonExtract("$.price.2.price_square", IntSqlType)

        val area get() = this.flat.jsonExtract("$.area", IntSqlType)
        val city get() = this.flat.jsonExtract("$.city_name", VarcharSqlType)
        val urban get() = this.flat.jsonExtract("$.urban_name", VarcharSqlType)
        val district get() = this.flat.jsonExtract("$.district_name", VarcharSqlType)
        val street get() = this.flat.jsonExtract("$.street_id", VarcharSqlType)
        val floor get() = this.flat.jsonExtract("$.floor", IntSqlType)
        val totalFloors get() = this.flat.jsonExtract("$.total_floors", IntSqlType)
        val rooms get() = this.flat.jsonExtract("$.room", VarcharSqlType).cast(IntSqlType)
        val lan get() = this.flat.jsonExtract("$.lat", DoubleSqlType)
        val lng get() = this.flat.jsonExtract("$.lng", DoubleSqlType)
        val favorite get() = this.flat.jsonExtract("$.favorite", BooleanSqlType)
    }

    private fun createTableIfNotExistQuery(table: BaseTable<*>): String {
        val columns = table.columns.map {
            "${it.name} ${it.sqlType.typeName} ${if (it in table.primaryKeys) "PRIMARY KEY" else ""}"
        }
            .joinToString(", ")
        return "CREATE TABLE IF NOT EXISTS ${table.tableName} ($columns);"
    }

    private fun createTableIfNotExist(table: BaseTable<*>) = connection.useConnection { conn ->
        val sql = createTableIfNotExistQuery(table)
        conn.prepareStatement(sql).use { it.executeUpdate() }
    }


    fun migrateData() {
        val flats = getFlats()
        flats.forEach {
            insertFlat(it)
        }
    }

    init {
        this.createTableIfNotExist(FlatTable)
    }

    fun insertFlats(flats: List<Response.Flat>) {
        connection.useTransaction { flats.forEach {
                insertFlat(it)
            }
        }
    }

    fun insertFlat(flat: Response.Flat) {
        try {
            connection.insertOrUpdate(FlatTable) {
                set(it.id, flat.id)
                set(it.flat, flat)
                onConflict{
//                    set(it.flat, flat)
                    doNothing()
                }
            }
        } catch (e: SQLException) {
            System.err.println(e.message)
        }
    }

    fun getFavoriteFlats(): List<Response.Flat> {
        return connection.from(FlatTable)
            .select(FlatTable.flat)
            .where(FlatTable.favorite eq true)
            .map {
                it[FlatTable.flat]!!
            }.toList()
    }

//    fun setFavorite(id: Int, favorite: Boolean = true) {
//        connection.update(FlatTable) {
//            set(FlatTable.favorite, favorite)
//            where {
//                it.id eq id
//            }
//        }
//    }


    fun getFlats(): List<Response.Flat> {
        return connection.from(FlatTable)
            .select(FlatTable.flat)
            .limit(100)
            .orderBy(FlatTable.lastUpdated.desc())
            .map {
                it[FlatTable.flat]!!
            }.toList()
    }

    fun getFlats(filter: Filter): List<Response.Flat> {
        val flats:MutableList<Response.Flat> = mutableListOf()
        val query =connection.from(FlatTable).select(FlatTable.flat)
            .whereWithConditions {expr ->
                filter.areaFrom.value?.let {expr += FlatTable.area.greaterEq(it)}
                filter.areaTo.value?.let {expr += FlatTable.area.lt(it)}
                filter.floorFrom.value?.let {expr += FlatTable.floor.gt(it)}
                filter.floorTo.value?.let {expr += FlatTable.floor.lt(it)}
                filter.totalFloorsFrom.value?.let {expr += FlatTable.totalFloors.greaterEq(it)}
                filter.totalFloorsTo.value?.let {expr += FlatTable.totalFloors.gt(it)}
                filter.priceFrom.value?.let {expr += FlatTable.price.greaterEq(it)}
                filter.priceTo.value?.let {expr += FlatTable.price.lt(it)}
                filter.roomsFrom.value?.let {expr += FlatTable.rooms.greaterEq(it)}
                filter.roomsTo.value?.let {expr += FlatTable.rooms.lt(it)}

                filter.city.value?.let {expr += FlatTable.city.inList(it)}
                filter.district.value?.let {expr += FlatTable.district.inList(it)}
                filter.urban.value?.let {expr += FlatTable.urban.inList(it)}
                filter.street.value?.let {expr += FlatTable.street.inList(it)}
            }
            .limit(filter.limit.value)
            .orderBy(FlatTable.lastUpdated.desc())

            query.forEach {
                flats +=it[FlatTable.flat]!!
            }
        return flats
    }

    fun getFlat(id: Int): Response.Flat? {
        connection.from(FlatTable).select(FlatTable.flat)
            .where(FlatTable.id eq id)
            .forEach {
                return it[FlatTable.flat]
            }
        return null
    }
}


