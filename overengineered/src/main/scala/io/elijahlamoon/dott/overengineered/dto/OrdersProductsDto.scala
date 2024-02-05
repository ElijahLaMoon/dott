package io.elijahlamoon.dott.overengineered.dto

import java.util.UUID

// Since Item is just a wrapper with additional info around Product,
// it makes sense to map their UUIDs directly, bypassing Item
final case class OrdersProductsDto(orderUuid: UUID, productUuid: UUID)
