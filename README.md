Cresco-Agent-Controller-Plugin
==============================

### Status
[![Build Status](http://128.163.188.129:9998/buildStatus/icon?job=Cresco-Controller)](http://128.163.188.129:9998/job/Cresco-Controller/)

---
### Install

1. Download/update/confirm a [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/overview/index.html) (JRE, Java Runtime) 1.6 or greater.
2. Download/update/confirm a [RabbitMQ](http://www.rabbitmq.com) Enviorment 3.3.x or greater.
3. Download the [Latest Build](http://128.163.188.129:9998/job/Cresco-Controller/lastSuccessfulBuild/com.researchworx$cresco-controller/) of the Cresco-Controller. 
4. Copy _Cresco-Controller.ini.sample_ to _Cresco-Controller.ini_
5. Modify _Cresco-Controller.ini_ for your enviorment.
6. Execute: java -jar  _cresco-controller-\<version\>.jar_ -f _\<location of configuration file\>_

---

### Usage

1. SSH to the hostname of the controller server.
2. Login using account: _admin_ and password: _admin_ **(actually any account will work as long as username matches password, this must be corrected soon)**
3. Follow shell instructions.

---

###License

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

###Project lead

Cody Bumgardner (@codybum)
