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

package org.kiji.maven.plugins;

import static junit.framework.Assert.assertTrue;

import java.net.URI;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

/**
 * Run this integration test against the bento-maven-plugin to demonstrate that the plugin works.
 * // TODO: Test access HDFS, HBase, etc.
 */
public class IntegrationTestSimple {
  @Test
  public void testHDFS() throws Exception {
    // Read hadoop configuration from the command line.
    final Configuration conf = new Configuration();

    final FileSystem hdfs = FileSystem.get(new URI("hdfs://"), conf);

    // Collect directory names.
    final Set<String> directoryNames = Sets.newHashSet();
    for (final FileStatus file : hdfs.listStatus(new Path("/"))) {
      directoryNames.add(file.getPath().getName());
    }
    assertTrue("Remote HDFS must have /hbase/", directoryNames.contains("hbase"));
    assertTrue("Remote HDFS must have /user/", directoryNames.contains("user"));
    assertTrue("Remote HDFS must have /var/", directoryNames.contains("var"));
    assertTrue("Remote HDFS must have /tmp", directoryNames.contains("tmp"));
  }
}
