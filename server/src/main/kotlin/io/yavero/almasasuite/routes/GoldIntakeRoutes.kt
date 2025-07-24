package io.yavero.almasasuite.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.yavero.almasasuite.models.*
import io.yavero.almasasuite.plugins.getManagerPinHeader
import io.yavero.almasasuite.plugins.verifyManagerPin
import io.yavero.almasasuite.plugins.getUserIdFromAuth
import io.yavero.almasasuite.services.GoldIntakeService


fun Route.goldIntakeRoutes() {
    val goldIntakeService = GoldIntakeService()

    route("/gold-intake") {

        get("/vendors") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val vendors = goldIntakeService.getAllVendors()
            call.respond(vendors)
        }


        get("/vendors/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing vendor ID")
            )


            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val vendor = goldIntakeService.getVendorById(id)
            if (vendor != null) {
                call.respond(vendor)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vendor not found"))
            }
        }


        post("/intakes") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val intakeRequest = call.receive<GoldIntakeRequest>()


            val validationError = goldIntakeService.validateGoldIntakeRequest(intakeRequest)
            if (validationError != null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to validationError)
                )
            }

            try {
                val managerPinHeader = call.request.headers[getManagerPinHeader()]
                val recordedBy = call.application.getUserIdFromAuth(managerPinHeader)
                val intake = goldIntakeService.recordGoldIntake(intakeRequest, recordedBy)
                call.respond(HttpStatusCode.Created, intake)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to record gold intake")
                )
            }
        }


        get("/intakes") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val intakes = goldIntakeService.getAllGoldIntakes()
            call.respond(intakes)
        }


        post("/payments") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val paymentRequest = call.receive<VendorPaymentRequest>()


            val validationError = goldIntakeService.validateVendorPaymentRequest(paymentRequest)
            if (validationError != null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to validationError)
                )
            }

            try {
                val managerPinHeader = call.request.headers[getManagerPinHeader()]
                val recordedBy = call.application.getUserIdFromAuth(managerPinHeader)
                val payment = goldIntakeService.recordVendorPayment(paymentRequest, recordedBy)
                call.respond(HttpStatusCode.Created, payment)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to record vendor payment")
                )
            }
        }


        get("/payments") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val vendorId = call.request.queryParameters["vendorId"]
            val payments = goldIntakeService.getVendorPayments(vendorId)
            call.respond(payments)
        }


        get("/liability-report") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val liabilityReport = goldIntakeService.getLiabilityReport()
            call.respond(liabilityReport)
        }


        post("/reduce-liability") {

            val managerPinHeader = call.request.headers[getManagerPinHeader()]
            if (!call.application.verifyManagerPin(managerPinHeader)) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Manager access required")
                )
            }

            val request = call.receive<ReduceLiabilityRequest>()

            if (request.vendorId.isBlank() || request.metalValueSold <= 0) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid vendor ID or metal value")
                )
            }

            try {
                val success = goldIntakeService.reduceVendorLiabilityForSale(
                    request.vendorId,
                    request.metalValueSold
                )

                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Liability reduced successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vendor not found"))
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to reduce liability")
                )
            }
        }
    }
}


@kotlinx.serialization.Serializable
data class ReduceLiabilityRequest(
    val vendorId: String,
    val metalValueSold: Double
)