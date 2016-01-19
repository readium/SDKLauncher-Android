//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification,
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//  this list of conditions and the following disclaimer in the documentation and/or
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be
//  used to endorse or promote products derived from this software without specific
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
//  OF THE POSSIBILITY OF SUCH DAMAGE

package org.readium.sdk.android.launcher.util;

import org.readium.sdk.android.util.ResourceInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ByteRangeInputStream extends InputStream {
    protected final ResourceInputStream ris;
    private final Object criticalSectionSynchronizedLock;

    private long requestedOffset = 0;
    private long alreadyRead = 0;
    private boolean isRange;

    private boolean isOpen = true;

    public ByteRangeInputStream(ResourceInputStream is, boolean isRange,
                                Object lock) {
        this.isRange = isRange;
        ris = is;
        criticalSectionSynchronizedLock = lock;
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
        synchronized (criticalSectionSynchronizedLock) {
            ris.close();
        }
    }

    @Override
    public int read() throws IOException {
        if (isOpen) {
            byte[] buffer = new byte[1];
            if (read(buffer) == 1) {
                return buffer[0];
            }
        }
        return -1;
    }

    public int available() throws IOException {
        int available;
        synchronized (criticalSectionSynchronizedLock) {
            available = ris.available();
        }
        long remaining = available - alreadyRead;
        if (remaining < 0) {
            remaining = 0;
        }
        return (int) remaining;
    }

    @Override
    public long skip(long byteCount) throws IOException {
        if (isRange) {
            requestedOffset = alreadyRead + byteCount;
        } else if (byteCount != 0) {
            synchronized (criticalSectionSynchronizedLock) {
                return ris.skip(byteCount);
            }
        }
        return byteCount;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (isRange) {
            requestedOffset = 0;
            alreadyRead = 0;
        } else {
            synchronized (criticalSectionSynchronizedLock) {
                ris.reset();
            }
        }
    }

    @Override
    public int read(byte[] b, int offset, int len) throws IOException {
        if (offset != 0) {
            throw new IOException("Offset parameter can only be zero");
        }
        if (len == 0 || !isOpen) {
            return -1;
        }
        int read;

        synchronized (criticalSectionSynchronizedLock) {

            if (isRange) {

                read = (int) ris.getRangeBytesX(requestedOffset
                        + alreadyRead, (long) len, b);

            } else {
                read = (int) ris.readX((long) len, b);
            }
        }

        alreadyRead += read;
        if (read == 0) {
            read = -1;
        }
        return read;
    }
}
