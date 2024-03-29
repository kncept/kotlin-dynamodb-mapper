package com.kncept.mapper.java.math

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.kncept.mapper.TypeMapper
import java.math.BigInteger
import kotlin.reflect.KClass

class BigIntegerMapper : TypeMapper<BigInteger> {
  override fun type(): KClass<BigInteger> {
    return BigInteger::class
  }

  override fun attributeType(): KClass<out AttributeValue> {
    return AttributeValue.N::class
  }

  override fun toItem(attribute: AttributeValue): BigInteger {
    if (attribute is AttributeValue.S) return BigInteger(attribute.asS())
    return BigInteger(attribute.asN())
  }

  override fun toAttribute(item: BigInteger): AttributeValue {
    return AttributeValue.N(item.toString())
  }
}
