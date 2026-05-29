/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.util.client;

import com.railwayteam.railways.Railways;

import java.util.Locale;

public final class ShutdownWatchdog {
    private static final long TIMEOUT_SECONDS =
        Long.getLong("railways.shutdownWatchdogSeconds", 8L);

    private ShutdownWatchdog() {}

    public static void arm() {
        if (TIMEOUT_SECONDS <= 0)
            return;
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("linux"))
            return;
        Runtime.getRuntime().addShutdownHook(
            new Thread(ShutdownWatchdog::onShutdown, "Railways NVIDIA shutdown watchdog"));
        Railways.LOGGER.info("[watchdog] armed (pid={}, timeout={}s)",
            ProcessHandle.current().pid(), TIMEOUT_SECONDS);
    }

    private static void onShutdown() {
        long pid = ProcessHandle.current().pid();
        Railways.LOGGER.info("[watchdog] shutdown started; force-killing pid {} if still alive in {}s",
            pid, TIMEOUT_SECONDS);
        try {
            new ProcessBuilder("sh", "-c",
                "sleep " + TIMEOUT_SECONDS + "; kill -0 " + pid + " 2>/dev/null && kill -9 " + pid)
                .start();
        } catch (Exception e) {
            Railways.LOGGER.error("[watchdog] failed to spawn external killer", e);
        }
    }
}
