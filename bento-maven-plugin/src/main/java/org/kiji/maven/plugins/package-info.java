/**
 * (c) Copyright 2014 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Package containing a Maven plugin to start and stop a Bento cluster in a Docker container for
 * use in integration testing.
 *
 * Goals:
 * <ul>
 *   <li>start</li>
 *   <li>stop</li>
 * </ul>
 *
 * Flags:
 * <table>
 *   <tr>
 *     <td>bento.skip</td>
 *     <td>Skips all goals of the bento-maven-plugin.</td>
 *   </tr>
 *   <tr>
 *     <td>bento.skip.create</td>
 *     <td>
 *       Skips creating the bento instance. Should be used in conjunction with an externally created
 *       bento instance through the "bento.name" property.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>bento.skip.start</td>
 *     <td>
 *       Skips starting the bento instance. Should be used in conjunction with an externally created
 *       and started bento instance through the "bento.name" property.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>bento.skip.stop</td>
 *     <td>Skips stopping the bento instance.</td>
 *   </tr>
 *   <tr>
 *     <td>bento.skip.rm</td>
 *     <td>Skips deleting the bento instance.</td>
 *   </tr>
 *   <tr>
 *     <td>bento.conf.dir</td>
 *     <td>
 *       Directory to place configuration files in. Defaults to the test-classes so that
 *       configuration files are on the classpath.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>bento.venv</td>
 *     <td>Python venv root to install the bento cluster to.</td>
 *   </tr>
 * </table>
 */
package org.kiji.maven.plugins;
