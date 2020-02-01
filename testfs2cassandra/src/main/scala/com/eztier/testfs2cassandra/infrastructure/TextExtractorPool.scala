package com.eztier
package testfs2cassandra.infrastructure

import org.apache.commons.pool2.impl.{DefaultPooledObject, GenericObjectPool, GenericObjectPoolConfig}
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject

class TextExtractorPool(factory: TextExtractorFactory, config: GenericObjectPoolConfig[TextExtractor]) extends GenericObjectPool[TextExtractor](factory, config) {

}

class TextExtractorFactory extends BasePooledObjectFactory[TextExtractor] {
  override def create(): TextExtractor = TextExtractor()

  override def wrap(obj: TextExtractor): PooledObject[TextExtractor] = new DefaultPooledObject[TextExtractor](obj)

  // override def passivateObject(p: PooledObject[TextExtractor]): Unit = super.passivateObject(p)
}
