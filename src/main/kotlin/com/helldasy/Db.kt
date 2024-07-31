package com.helldasy

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

fun main() {
    val db = Db()
    db.migrateData()
}

class Db(path: String = "./flats") {
    private val connection: Connection = DriverManager.getConnection("jdbc:h2:$path")

    fun init() {
        try {
            val createTableQuery = listOf(
                "CREATE TABLE IF NOT EXISTS flats (id integer PRIMARY KEY, flat json not null);",
                "ALTER TABLE PUBLIC.FLATS ADD PRICE_TOTAL INTEGER;",
                "ALTER TABLE PUBLIC.FLATS ADD PRICE_SQUARE INTEGER;",
                "ALTER TABLE PUBLIC.FLATS ADD STREET_ID INTEGER;",
                "ALTER TABLE PUBLIC.FLATS ADD TOTAL_FLOORS INTEGER;",
                "ALTER TABLE PUBLIC.FLATS ADD FLOOR INTEGER;",
                "ALTER TABLE PUBLIC.FLATS ADD ROOM INTEGER;",
                "ALTER TABLE PUBLIC.FLATS ADD LAT REAL;",
                "ALTER TABLE PUBLIC.FLATS ADD LNG REAL;",
                "ALTER TABLE PUBLIC.FLATS ADD AREA REAL;",
                "ALTER TABLE PUBLIC.FLATS ADD LAST_UPDATED TIMESTAMP;",
            )
            val statement = connection.createStatement()
            createTableQuery.forEach { statement.execute(it) }
            statement.close()
        } catch (e: SQLException) {
            System.err.println(e.message)
        }
    }

    fun migrateData() {
        val flats = getFlats()
        flats.forEach {
            insertFlat(it)
        }
    }

    fun insertFlats(flats: List<Response.Flat>) {
        connection.autoCommit = false
        flats.forEach {
            insertFlat(it)
        }
        connection.commit()
        connection.autoCommit = true

    }

    fun insertFlat(flat: Response.Flat) {
        try {
            val insertQuery =
                "MERGE INTO flats (id, flat, PRICE_TOTAL, PRICE_SQUARE, STREET_ID, TOTAL_FLOORS,FLOOR,ROOM,LAT,LNG,AREA, LAST_UPDATED) " +
                        "VALUES (?, ? FORMAT JSON , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
            val statement = connection.prepareStatement(insertQuery)
                .apply {
                    setInt(1, flat.id)
                    setString(2, flat.toFlatString())
                    set(3, flat.price["2"]?.price_total)
                    set(4, flat.price["2"]?.price_square)
                    set(5, flat.street_id)
                    set(6, flat.total_floors)
                    set(7, flat.floor)
                    set(8, flat.room)
                    set(9, flat.lat)
                    set(10, flat.lng)
                    set(11, flat.area)
                    setObject(12, flat.last_updated?.toDate(), java.sql.Types.TIMESTAMP)
                }
            statement.execute()
        } catch (e: SQLException) {
            System.err.println(e.message)
        }
    }

    private fun PreparedStatement.set(position: Int, value: Double?) {
        this.setObject(position, value, java.sql.Types.REAL)
    }

    private fun PreparedStatement.set(position: Int, value: Int?) {
        this.setObject(position, value, java.sql.Types.INTEGER)
    }

    private fun PreparedStatement.set(position: Int, value: String?) {
        if (value?.all { it.isDigit() } == true)
            this.setObject(position, value, java.sql.Types.INTEGER)
    }

    fun getFlats(): List<Response.Flat> {
        val selectQuery = "SELECT * from FLATS order by LAST_UPDATED DESC limit 100"
        return getFlats(selectQuery)
    }

    fun getFlats(query: String): List<Response.Flat> {
        val flats = mutableListOf<Response.Flat>()
        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                val flat = resultSet.getString("flat")
                flats.add(json.decodeFromString(Response.Flat.serializer(), flat))
            }
            statement.close()
        } catch (e: SQLException) {
            System.err.println(e.message)
        }
        return flats
    }

    fun getFlat(id: Int): Response.Flat? {
        try {
            val selectQuery = "SELECT flat FROM flats WHERE id = $id;"
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery(selectQuery)
            if (resultSet.next()) {
                val flat = resultSet.getString("flat")
                return json.decodeFromString(Response.Flat.serializer(), flat)
            }
            statement.close()
        } catch (e: SQLException) {
            System.err.println(e.message)
        }
        return null
    }
}


