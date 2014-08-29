Bento Cluster Plugin ${project.version}
=======================================

A Maven plugin to start and stop a Bento cluster running in a Docker container to be used in integration testing.

See the [bento cluster project](https://github.com/kijiproject/bento-cluster) for more on the Bento running in Docker.

Usage
-----

Ensure Docker is installed and that the user is in the `docker` user group.

Bento cluster can be installed by running:

    pip3 install kiji-bento-cluster

Pull the latest bento image:

    bento pull

Ensure that the `maven-failsafe-plugin` and the `bento-maven-plugin` are part of your module's
`pom.xml`, by inserting the following block:

    <plugin>
      <artifactId>maven-failsafe-plugin</artifactId>
      <version>2.17</version>
      <executions>
        <execution>
          <goals>
            <goal>integration-test</goal>
            <goal>verify</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    <plugin>
      <groupId>org.kiji.maven.plugins</groupId>
      <artifactId>bento-maven-plugin</artifactId>
      <executions>
        <execution>
          <goals>
            <goal>start</goal>
            <goal>stop</goal>
          </goals>
        </execution>
      </executions>
    </plugin>


The plugin exposes the following properties for adjusting its behavior:

<table>
  <tr>
    <td>Property</td>
    <td>Type</td>
    <td>Description</td>
  </tr>
  <tr>
    <td><pre><code>bento.docker.address</code></pre></td>
    <td>string</td>
    <td>Address of the docker daemon to use to manage bento instances.</td>
  </tr>
  <tr>
    <td><pre><code>bento.skip</code></pre></td>
    <td>boolean</td>
    <td>Skips all goals of the bento-maven-plugin.</td>
  </tr>
  <tr>
    <td><pre><code>bento.skip.create</code></pre></td>
    <td>boolean</td>
    <td>
      Skips creating the bento instance. Should be used in conjunction with an externally created
      bento instance through the "bento.name" property.
    </td>
  </tr>
  <tr>
    <td><pre><code>bento.skip.start</code></pre></td>
    <td>boolean</td>
    <td>
      Skips starting the bento instance. Should be used in conjunction with an externally created
      and started bento instance through the "bento.name" property.
    </td>
  </tr>
  <tr>
    <td><pre><code>bento.skip.stop</code></pre></td>
    <td>boolean</td>
    <td>Skips stopping the bento instance.</td>
  </tr>
  <tr>
    <td><pre><code>bento.skip.rm</code></pre></td>
    <td>boolean</td>
    <td>Skips deleting the bento instance.</td>
  </tr>
  <tr>
    <td><pre><code>bento.conf.dir</code></pre></td>
    <td>string</td>
    <td>
      Directory to place configuration files in. Defaults to the test-classes so that
      configuration files are on the classpath.
    </td>
  </tr>
  <tr>
    <td><pre><code>bento.venv</code></pre></td>
    <td>string</td>
    <td>Python venv root to install the bento cluster to.</td>
  </tr>
  <tr>
    <td><pre><code>bento.pypi.repository</code></pre></td>
    <td>string</td>
    <td>Pypi repository to install kiji-bento-cluster from.</td>
  </tr>
</table>


These properties can be specified on the command line by placing a -D in front of the property name
and adding a '=' followed by its value:

    -Dproperty.name="property value"
