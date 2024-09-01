package com.helldaisy


import com.helldaisy.Db.FlatTable
import com.helldaisy.ui.Filter
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.expression.*
import org.ktorm.jackson.json
import org.ktorm.schema.*
import org.ktorm.support.sqlite.*
import java.nio.file.Paths
import java.sql.SQLException
import java.time.LocalDateTime


fun main() {
    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
    val db = Db(path)
//    println(path)
//    val a =FlatTable.lastUpdated.gt(LocalDateTime.now().minusDays(1))
    val a =
        BinaryExpression(BinaryExpressionType.GREATER_THAN,
            FlatTable.lastUpdated.asExpression(),
            ArgumentExpression(LocalDateTime.now().minusDays(30).toString(), VarcharSqlType),
            BooleanSqlType)

    val query = db.connection.from(FlatTable).select(FlatTable.flat)
        .where(a)
        .orderBy(FlatTable.lastUpdated.asc())
        .limit(5)

        println(query.sql)

        query.forEach {
            println(it[FlatTable.flat]?.last_updated)
        }

}

class Db(path: String = "./flats") {
    val connection = Database.connect("jdbc:sqlite:$path.db",
        dialect = SQLiteDialect())

    object FlatTable : Table<Nothing>("flats") {
        val id = int("id").primaryKey()
        val flat = json<Response.Flat>("flat")
        val lastUpdated: FunctionExpression<LocalDateTime>
            get() = FunctionExpression(
                functionName = "datetime",
                arguments = listOf(this.flat.jsonExtract<String>("$.last_updated", VarcharSqlType).asExpression()),
                sqlType = LocalDateTimeSqlType
            )
        val price get() = this.flat.jsonExtract("$.price.2.price_total", IntSqlType)
        val priceSquare get() = this.flat.jsonExtract("$.price.2.price_square", IntSqlType)
        val removeFavorite = this.flat.jsonRemove("$.favorite")
        val area get() = this.flat.jsonExtract("$.area", IntSqlType)
        val city get() = this.flat.jsonExtract("$.city_name", VarcharSqlType).toLowerCase()
        val urbanId get() = this.flat.jsonExtract("$.urban_id", IntSqlType)
        val districtId get() = this.flat.jsonExtract("$.district_id", IntSqlType)

        val street get() = this.flat.jsonExtract("$.street_id", VarcharSqlType)
        val floor get() = this.flat.jsonExtract("$.floor", IntSqlType)
        val totalFloors get() = this.flat.jsonExtract("$.total_floors", IntSqlType)
        val rooms get() = this.flat.jsonExtract("$.room", VarcharSqlType).cast(IntSqlType)
        val lan get() = this.flat.jsonExtract("$.lat", DoubleSqlType)
        val lng get() = this.flat.jsonExtract("$.lng", DoubleSqlType)
        val favorite get() = this.flat.jsonExtract("$.favorite", BooleanSqlType)

    }

    fun ColumnDeclaring<*>.jsonSet(right: ColumnDeclaring<*>): FunctionExpression<String> {
        // json_patch(left, right)
        return FunctionExpression(
            functionName = "json_set",
            arguments = listOf(this, right).map { it.asExpression() },
            sqlType = VarcharSqlType
        )
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
        println("Insert flats ${flats.size}")
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
                    set(it.flat, flat)
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

    fun getFlats(filter: Filter ): List<Response.Flat> {
        val flats:MutableList<Response.Flat> = mutableListOf()
        val query =connection.from(FlatTable).select(
            FlatTable.flat
        )
            .whereWithConditions {expr ->
                filter.areaFrom.value?.let {expr += FlatTable.area.greaterEq(it)}
                filter.areaTo.value?.let {expr += FlatTable.area.lt(it)}
                filter.floorFrom.value?.let {expr += FlatTable.floor.gt(it)}
                filter.floorTo.value?.let {expr += FlatTable.floor.lt(it)}
                filter.totalFloorsFrom.value?.let {expr += FlatTable.totalFloors.greaterEq(it)}
                filter.totalFloorsTo.value?.let {expr += FlatTable.totalFloors.gt(it)}


                filter.cities.value.let {cityIds ->
                    if (cityIds.isNotEmpty()) {
                        val cityNames =cityIds.mapNotNull { cityId -> locationsCl.cities[cityId] }
                        expr += FlatTable.city.inList(cityNames)
                        filter.districts.value.let { districts ->
                            if (districts.isNotEmpty()) {
                                expr += FlatTable.districtId.inList(districts)
                                filter.urbans.value.let { urbans ->
                                    if (urbans.isNotEmpty()) {
                                        expr += FlatTable.urbanId.inList(urbans)
                                    }
                                }
                            }
                        }

                    }
                }
                filter.street.value?.let {expr += FlatTable.street.inList(it)}
                filter.lastUpdated.value.let {expr +=
                    BinaryExpression(BinaryExpressionType.GREATER_THAN,
                        FlatTable.lastUpdated.asExpression(),
                        ArgumentExpression(LocalDateTime.now().minusDays(it.toLong()).toString(), VarcharSqlType),
                        BooleanSqlType)}
                filter.lanFrom.value?.let {expr += FlatTable.lan.greaterEq(it)}
                filter.lanTo.value?.let {expr += FlatTable.lan.lt(it)}
                filter.lngFrom.value?.let {expr += FlatTable.lng.greaterEq(it)}
                filter.lngTo.value?.let {expr += FlatTable.lng.lt(it)}
            }
            .limit(filter.limit.value)
            .orderBy(FlatTable.lastUpdated.desc())
            val time = System.currentTimeMillis()
            query.forEach {
                flats +=it[FlatTable.flat]!!
            }
            println("Query time: ${System.currentTimeMillis() - time}")
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
