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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BentoTestUtils {
  private static final Logger LOG = LoggerFactory.getLogger(BentoTestUtils.class);

  public static final RetryOneTime CURATOR_RETRY_POLICY = new RetryOneTime(1000);

  /** Disabled constructor for utility class. */
  private BentoTestUtils() { }

  public static void validateBentoName(
      final File configDir,
      final String bentoName
  ) throws IOException {
    // Validate that the bento name was stored correctly.
    final Properties bentoProperties = new Properties();
    final File bentoPropertiesFile = new File(configDir, BentoCluster.BENTO_NAME_FILE);
    final FileInputStream inputStream = new FileInputStream(bentoPropertiesFile);
    try {
      bentoProperties.load(inputStream);
    } finally {
      inputStream.close();
    }

    Assert.assertEquals(bentoName, bentoProperties.get(BentoCluster.BENTO_NAME_PROPERTY));
  }

  static void validateHdfs(
      final Configuration conf
  ) throws URISyntaxException, IOException {
    LOG.info("Validating that hdfs has started correctly.");

    final URI bentoHdfsUri = new URI("hdfs:///");
    final FileSystem hdfs = FileSystem.get(bentoHdfsUri, conf);

    // Collect directory names.
    final Set<String> directoryNames = Sets.newHashSet();
    for (final FileStatus file : hdfs.listStatus(new Path("/"))) {
      directoryNames.add(file.getPath().getName());
    }
    Assert.assertTrue("Bento HDFS must have /hbase/", directoryNames.contains("hbase"));
    Assert.assertTrue("Bento HDFS must have /user/", directoryNames.contains("user"));
    Assert.assertTrue("Bento HDFS must have /var/", directoryNames.contains("var"));
    Assert.assertTrue("Bento HDFS must have /tmp", directoryNames.contains("tmp"));

    // Try writing to hdfs.
    final Path randomDirectory = new Path(UUID.randomUUID().toString());
    Assert.assertTrue("Failed to create directory on hdfs.", hdfs.mkdirs(randomDirectory));
    Assert.assertTrue("Failed to create directory on hdfs.", hdfs.isDirectory(randomDirectory));
  }

  static void validateZookeeper(final String bentoName) throws Exception {
    LOG.info("Validating that zookeeper has started correctly.");

    final CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
        .connectString(String.format("%s:2181", bentoName))
        .retryPolicy(CURATOR_RETRY_POLICY)
        .build();
    try {
      curatorFramework.start();

      final Stat zookeeperStat = curatorFramework.checkExists().forPath("/zookeeper");
      Assert.assertNotNull("Bento zookeeper must have a /zookeeper path", zookeeperStat);
    } finally {
      curatorFramework.close();
    }
  }

  static Configuration bentoConfiguration(final File configDir) {
    final Configuration conf = new Configuration(false);
    final File coreSite = new File(configDir, "core-site.xml");
    final File yarnSite = new File(configDir, "yarn-site.xml");
    final File mapredSite = new File(configDir, "mapred-site.xml");
    final File hbaseSite = new File(configDir, "hbase-site.xml");
    conf.addResource(new Path(String.format("file://%s", coreSite.getAbsolutePath())));
    conf.addResource(new Path(String.format("file://%s", yarnSite.getAbsolutePath())));
    conf.addResource(new Path(String.format("file://%s", mapredSite.getAbsolutePath())));
    conf.addResource(new Path(String.format("file://%s", hbaseSite.getAbsolutePath())));
    return conf;
  }
}
