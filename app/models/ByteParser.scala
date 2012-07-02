package models

import anorm.{TypeDoesNotMatch, MetaDataItem, Column}

/**
 * Created with IntelliJ IDEA.
 * User: Ryan
 * Date: 7/1/12
 * Time: 9:02 PM
 * To change this template use File | Settings | File Templates.
 */

trait ByteParser {
  // magic from google group
  implicit def rowToByte: Column[Byte] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case byte: Byte => Right(byte)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Byte for column " + qualified))
    }
  }
}
