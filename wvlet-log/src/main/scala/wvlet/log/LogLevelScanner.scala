/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wvlet.log

import java.io.File
import java.util.Properties
import java.util.concurrent.{Executors, ThreadFactory, TimeUnit}

import wvlet.log.io.{IOUtil, Resource}

import scala.concurrent.duration.Duration

private[log] class LogLevelScanner(val loglevelFileCandidates: Seq[String], val scanInterval: Duration) extends Runnable {
  private val logger = Logger("wvlet.log")

  private val executor = Executors.newScheduledThreadPool(1, new ThreadFactory {
    override def newThread(r: Runnable): Thread = {
      val t = new Thread(r)
      t.setName("LogLevelScanner")
      // Enable terminating JVM without shutting down this executor
      t.setDaemon(true)
      t
    }
  })

  def start() {
    executor.scheduleAtFixedRate(this, 0, scanInterval.toMillis, TimeUnit.MILLISECONDS)
  }

  def stop() {
    // No need to wait the thread termination here
    executor.shutdown()
  }

  private var lastScanned : Option[Long] = None

  def run {
    try {
      val logFileURL = loglevelFileCandidates
                       .toStream
                       .flatMap(f => Resource.find(f))
                       .headOption

      logFileURL.map { url =>
        url.getProtocol match {
          case "file" =>
            val f = new File(url.toURI)
            val lastModified = f.lastModified()
            if (lastScanned.isEmpty || lastScanned.get < lastModified) {
              Logger.setLogLevels(f)
              lastScanned = Some(System.currentTimeMillis())
            }
          case other =>
            // non file resources found in the class path is stable, so we only need to read it once
            if (lastScanned.isEmpty) {
              IOUtil.withResource(url.openStream()) { in =>
                val p = new Properties
                p.load(in)
                lastScanned = Some(System.currentTimeMillis())
                Logger.setLogLevels(p)
              }
            }
        }
      }
    }
    catch {
      case e:Throwable =>
        // We need to use the native java.util.logging.Logger since the logger macro cannot be used within the same project
        logger.wrapped.log(LogLevel.WARN.jlLevel,  s"Error occurred while scanning log properties: ${e.getMessage}", e)
    }
  }
}