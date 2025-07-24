package io.yavero.almasasuite.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.yavero.almasasuite.models.SaleRequest
import io.yavero.almasasuite.services.SalesService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun Route.salesRoutes() {
    val salesService = SalesService()

    route("/sales") {

        get {
            val sales = salesService.getAllSales()
            call.respond(sales)
        }


        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing id parameter")
            )

            val sale = salesService.getSaleById(id)
            if (sale != null) {
                call.respond(sale)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Sale not found"))
            }
        }


        post {

            val saleRequest = call.receive<SaleRequest>()


            val validationError = salesService.validateSaleRequest(saleRequest)
            if (validationError != null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to validationError)
                )
            }


            for (item in saleRequest.items) {
                val (available, currentStock) = salesService.checkProductAvailability(item.productId, item.quantity)
                if (!available) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf(
                            "error" to "Insufficient stock for product ${item.productId}",
                            "requested" to item.quantity,
                            "available" to currentStock
                        )
                    )
                }
            }


            val sale = salesService.createSale(saleRequest)
            call.respond(HttpStatusCode.Created, sale)
        }


        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing id parameter")
            )


            if (!salesService.saleExists(id)) {
                return@delete call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Sale not found")
                )
            }


            val deleted = salesService.deleteSale(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete sale"))
            }
        }


        get("/range") {
            val startDateStr = call.request.queryParameters["startDate"]
            val endDateStr = call.request.queryParameters["endDate"]

            if (startDateStr == null || endDateStr == null) {
                return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Both startDate and endDate parameters are required")
                )
            }

            try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val startDate = LocalDateTime.parse(startDateStr, formatter)
                val endDate = LocalDateTime.parse(endDateStr, formatter)

                if (startDate.isAfter(endDate)) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Start date must be before end date")
                    )
                }

                val sales = salesService.getSalesByDateRange(startDate, endDate)
                call.respond(sales)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss")
                )
            }
        }
    }
}