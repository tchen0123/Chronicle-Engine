/*
 * Copyright 2016 higherfrequencytrading.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.engine.fs;

import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.cfg.EngineClusterContext;
import net.openhft.chronicle.network.cluster.Cluster;
import net.openhft.chronicle.network.cluster.HostDetails;
import org.jetbrains.annotations.NotNull;

public class EngineCluster extends Cluster<HostDetails, EngineClusterContext> {

    public EngineCluster(String clusterName) {
        super(clusterName);
    }

    void assetRoot(@NotNull Asset assetRoot) {
        EngineClusterContext context = clusterContext();
        if (context != null)
            context.assetRoot(assetRoot);
    }

    @NotNull
    @Override
    protected HostDetails newHostDetails() {
        return new HostDetails();
    }
}
