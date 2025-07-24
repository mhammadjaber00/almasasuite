package io.yavero.almasasuite.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.yavero.almasasuite.models.*
import io.yavero.almasasuite.services.ProductService


fun Route.productRoutes() {
    val productService = ProductService()

    route("/products") {

        get {
            val products = productService.getAllProducts()
            call.respond(products)
        }


        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing id parameter")
            )

            val product = productService.getProductById(id)
            if (product != null) {
                call.respond(product)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))
            }
        }


        post {

            val productRequest = call.receive<ProductRequest>()


            val validationError = productService.validateProductRequest(productRequest)
            if (validationError != null) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to validationError)
                )
            }


            val sku = if (productRequest.sku.isNullOrBlank()) {
                productService.generateSku(productRequest.type, productRequest.karat, productRequest.weightGrams)
            } else {
                productRequest.sku
            }


            if (productService.skuExists(sku!!)) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "SKU already exists")
                )
            }


            val product = productService.createProduct(productRequest)
            call.respond(HttpStatusCode.Created, product)
        }


        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing id parameter")
            )

            val productRequest = call.receive<ProductRequest>()


            val validationError = productService.validateProductRequest(productRequest)
            if (validationError != null) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to validationError)
                )
            }


            if (!productService.productExists(id)) {
                return@put call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Product not found")
                )
            }


            val sku = if (productRequest.sku.isNullOrBlank()) {
                productService.generateSku(productRequest.type, productRequest.karat, productRequest.weightGrams)
            } else {
                productRequest.sku
            }


            if (productService.skuExistsForOtherProduct(sku!!, id)) {
                return@put call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "SKU already exists")
                )
            }


            val updatedProduct = productService.updateProduct(id, productRequest)
            if (updatedProduct != null) {
                call.respond(updatedProduct)
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update product"))
            }
        }


        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing id parameter")
            )


            if (!productService.productExists(id)) {
                return@delete call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Product not found")
                )
            }


            val deleted = productService.deleteProduct(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete product"))
            }
        }


        patch("/{id}/stock") {
            val id = call.parameters["id"] ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing id parameter")
            )

            val stockRequest = call.receive<StockUpdateRequest>()


            if (stockRequest.quantity < 0) {
                return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Quantity cannot be negative")
                )
            }


            if (!productService.productExists(id)) {
                return@patch call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Product not found")
                )
            }


            val updated = productService.updateStock(id, stockRequest.quantity)
            if (updated) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Stock updated successfully"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update stock"))
            }
        }
    }
}


@kotlinx.serialization.Serializable
data class StockUpdateRequest(
    val quantity: Int
)