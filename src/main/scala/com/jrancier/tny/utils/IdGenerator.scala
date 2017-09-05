package com.jrancier.tny.utils

/**
  * We represent our shortened urls as a set of alphanumeric characters
  * 0-9, a-z, and A-Z
  * This gives us 62 values per field.
  *
  * Our hash range can grow as our shortened IDs get larger. It'll
  * start at 'A' and grow in length as more and more URLs are added.
  * If prefered, we can pad them to a set length, but this method allows
  * for a virtually infinite number of shortened URLs as the hash length
  * can grow.
  *
  * Since the IDs must be unique
  * The most straight forward way to do so is to have serial IDs.
  * This is a bottleneck for a a 'sharded' application since you cannot
  * shard serial ID generation in a normal way. To work around this,
  * We can have a single server that provides 'chunks' of our serial
  * range that each of our sharded 'Tny' endpoint servers can use.
  * The large the chunks the singleton server provides, the less often
  * the shard servers will need to contact the singleton server to get
  * a new serial range. But the larger range we could lose when a shard
  * endpoint dies, since it will need to request a fresh chunk when it
  * comes back up.
  *
  * When scaling, if we find that our singleton server is under too much
  * load, we can increase the chunk size to reduce it's load.
  */
object IdGenerator {
  var chunkStart: Option[Int]
  var chunkSize: Option[Int]
  var chunkCurrentIndex = 0

  def GetNextId : Int = {
    if(chunkStart.isEmpty){
      getNextChunk
    }

  }

  def getNextChunk = {
    (chunkStart, chunkSize) = ChunkController.getNextChunkDetails
  }


}

/**
  * Convert to base
  */
object IDEncoder {
  val encodingString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  val encodingLenght = encodingString.length
  def encodeId(id: Int) : String = {
    var currentRemainder = id
    var result = ""

    while(currentRemainder != 0) {
      result += encodingString.charAt(currentRemainder % encodingLenght)
      currentRemainder /= encodingLenght
    }

    result
  }

}


/**
  * To start scaling, we'll move this object to it's own singleton server
  * to serve up serial chunks to our shards for use in shortening URLs
  */
object ChunkController {
  //load current chunk values from MongoDB
  var nextChunkStart : Int = 0
  //load chunkSize from config (dynamically?)
  var chunkSize = 100

  def getNextChunkDetails : ChunkDetails = {
    val details = ChunkDetails(nextChunkStart, chunkSize)
    nextChunkStart = nextChunkStart + chunkSize
    //Save to Mongo DB before giving up the current chunk
    details
  }

}

case class ChunkDetails(chunkStart : Int, chunkSize : Int)