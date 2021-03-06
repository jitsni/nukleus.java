/**
 * Copyright 2016-2017 The Reaktivity Project
 *
 * The Reaktivity Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.reaktivity.nukleus;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.ServiceLoader.load;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class ControllerFactory
{
    public static ControllerFactory instantiate()
    {
        return instantiate(load(ControllerFactorySpi.class));
    }

    public static ControllerFactory instantiate(ClassLoader classLoader)
    {
        return instantiate(load(ControllerFactorySpi.class, classLoader));
    }

    public Iterable<? extends Class<? extends Controller>> kinds()
    {
        return factorySpisByName.keySet();
    }

    public <T extends Controller> String name(Class<T> kind)
    {
        requireNonNull(kind, "kind");

        ControllerFactorySpi factorySpi = factorySpisByName.get(kind);
        if (factorySpi == null)
        {
            throw new IllegalArgumentException("Unregonized controller kind: " + kind.getName());
        }

        return factorySpi.name();
    }

    public <T extends Controller> T create(Class<T> kind, Configuration config)
    {
        requireNonNull(kind, "kind");
        requireNonNull(config, "config");

        ControllerFactorySpi factorySpi = factorySpisByName.get(kind);
        if (factorySpi == null)
        {
            throw new IllegalArgumentException("Unregonized controller kind: " + kind.getName());
        }

        return factorySpi.create(kind, config);
    }

    private static ControllerFactory instantiate(ServiceLoader<ControllerFactorySpi> factories)
    {
        Map<Class<? extends Controller>, ControllerFactorySpi> factorySpisByName = new HashMap<>();
        factories.forEach((factorySpi) -> factorySpisByName.put(factorySpi.kind(), factorySpi));

        return new ControllerFactory(unmodifiableMap(factorySpisByName));
    }

    private final Map<Class<? extends Controller>, ControllerFactorySpi> factorySpisByName;

    private ControllerFactory(Map<Class<? extends Controller>, ControllerFactorySpi> factorySpisByName)
    {
        this.factorySpisByName = factorySpisByName;
    }
}
