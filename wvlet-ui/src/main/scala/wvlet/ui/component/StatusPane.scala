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
package wvlet.ui.component
import mhtml._
/**
  *
  */
object StatusPane extends RxElement {

  val statusMessage = Var("wvlet status")

  override def body = {
    statusMessage.map { m =>
      <div class="alert alert-info alert-dismissible fade show" role="alert">
<!--        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
         -->
        <strong>status</strong>: {m.trim}
      </div>
    }
  }
}
