package com.r.bigconf.core.filter;

import java.nio.ByteBuffer;

public interface Filter {

    /**
     * Apply filter
     * @param unfiltered original source
     * @return filtered source
     */
    ByteBuffer filter(ByteBuffer unfiltered);
}
