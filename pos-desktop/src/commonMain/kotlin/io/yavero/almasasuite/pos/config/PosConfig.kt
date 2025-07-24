package io.yavero.almasasuite.pos.config

import java.io.File
import java.util.*


class PosConfig private constructor(private val properties: Properties) {


    val api = ApiConfig()


    val security = SecurityConfig()


    val database = DatabaseConfig()


    val tax = TaxConfig()


    inner class ApiConfig {


        val baseUrl: String
            get() = properties.getProperty("api.baseUrl", "http://localhost:8080/api")
    }


    inner class SecurityConfig {
        // Manager pin functionality has been removed
    }


    inner class DatabaseConfig {


        val path: String
            get() = properties.getProperty("database.path", "almasa.db")
    }


    inner class TaxConfig {


        val rate: Double
            get() = properties.getProperty("tax.rate", "0.1").toDouble()
    }

    companion object {
        private var instance: PosConfig? = null


        fun getInstance(): PosConfig {
            if (instance == null) {
                instance = loadConfig()
            }
            return instance!!
        }


        private fun loadConfig(): PosConfig {
            val properties = Properties()

            try {

                val configFile = File("pos-config.properties")
                if (configFile.exists()) {
                    configFile.inputStream().use { properties.load(it) }
                    println("Loaded configuration from ${configFile.absolutePath}")
                } else {
                    println("Configuration file not found, using default values")
                }
            } catch (e: Exception) {
                println("Error loading configuration: ${e.message}")
                e.printStackTrace()
            }

            return PosConfig(properties)
        }
    }
}