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

import com.google.common.base.Preconditions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A maven goal that stops the Bento cluster started by the 'start' goal.
 */
@Mojo(
    name = "stop",
    defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST
)
public class StopMojo extends AbstractMojo {
  /**
   * If true, this goal should be a no-op.
   */
  @Parameter(
      property = "bento.skip",
      alias = "bento.skip",
      defaultValue = "false",
      required = false
  )
  private boolean mSkip;

  /**
   * Optional bento instance name override. Can be used to use an existing bento instance.
   */
  @Parameter(
      property = "bento.name",
      alias = "bento.name",
      required = false
  )
  private String mBentoName;

  /**
   * Python venv root to install the bento cluster to.
   */
  @Parameter(
      property = "bento.venv",
      alias = "bento.venv",
      defaultValue = "${project.build.directory}/bento-maven-plugin-venv",
      required = false
  )
  private File mBentoVenvRoot;

  /**
   * If true, skips stopping the bento instance.
   */
  @Parameter(
      property = "bento.skip.stop",
      alias = "bento.skip.stop",
      defaultValue = "false",
      required = false
  )
  private boolean mSkipBentoStop;

  /**
   * If true, skips deleting the bento instance.
   */
  @Parameter(
      property = "bento.skip.rm",
      alias = "bento.skip.rm",
      defaultValue = "false",
      required = false
  )
  private boolean mSkipBentoRm;

  /** {@inheritDoc} */
  @Override
  public void execute() throws MojoExecutionException {
    if (mSkip) {
      getLog().info("Not stopping an Bento cluster because bento.skip=true.");
      return;
    }

    if (mSkipBentoStop) {
      getLog().info("Not stopping an Bento cluster because bento.skip.stop=true.");
      return;
    }

    // Stop the cluster.
    try {
      if (!BentoCluster.isInstanceSet()) {
        Preconditions.checkArgument(
            null != mBentoName,
            "A bento name must be provided if a bento wasn't started by this plugin."
        );
        BentoCluster.setInstance(mBentoName, mBentoVenvRoot, getLog());
      }
      BentoCluster.getInstance().stop(!mSkipBentoRm);
    } catch (final Exception e) {
      throw new MojoExecutionException("Unable to stop Bento cluster.", e);
    }
  }
}