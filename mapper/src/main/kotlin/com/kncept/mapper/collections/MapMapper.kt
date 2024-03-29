package com.kncept.mapper.collections

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.kncept.mapper.DynamoDbObjectMapper
import com.kncept.mapper.TypeMapper
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

class MapMapper(
    private val mapper: DynamoDbObjectMapper,
    private val javaMathNumericTypes: Boolean,
) : TypeMapper<Map<String, out Any>> {
  override fun type(): KClass<Map<String, out Any>> {
    return Map::class as KClass<Map<String, out Any>>
  }

  override fun toItem(attribute: AttributeValue): Map<String, out Any> {
    return attribute
        .asM()
        .map { entry -> entry.key to unboundAttributeToProbableType(entry.value) }
        .toMap()
  }

  override fun attributeType(): KClass<out AttributeValue> {
    return AttributeValue.M::class
  }

  override fun toAttribute(item: Map<String, out Any>): AttributeValue {
    return AttributeValue.M(
        item
            .map { entry ->
              val type = entry.value::class
              val typeMapper = mapper.typeMapper(type)!!
              entry.key to typeMapper.toAttribute(entry.value)
            }
            .toMap())
  }

  fun unboundAttributeToProbableType(value: AttributeValue): Any {
    return if (value is AttributeValue.M) toItem(value)
    else if (value is AttributeValue.S) value.asS()
    else if (value is AttributeValue.N) {
      val n = value.asN()
      if (javaMathNumericTypes) {
        if (n.contains(".")) mapper.typeMapper(BigDecimal::class)!!.toItem(value)
        else mapper.typeMapper(BigInteger::class)!!.toItem(value)
      } else {
        if (n.contains(".")) mapper.typeMapper(Double::class)!!.toItem(value)
        else mapper.typeMapper(Long::class)!!.toItem(value)
      }
          as Any
    } else if (value is AttributeValue.Bool) value.asBool()
    else if (value is AttributeValue.B) value.asB()
    else throw IllegalArgumentException("Only simple maps are supported")
  }
}
