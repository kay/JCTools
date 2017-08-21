/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jctools.proxy;

import static org.jctools.proxy.FixedMessageSizeRingBuffer.EOF;
import static org.jctools.proxy.FixedMessageSizeRingBuffer.MESSAGE_INDICATOR_SIZE;
import static org.jctools.util.UnsafeAccess.UNSAFE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.jctools.proxy.FixedMessageSizeRingBuffer;

public class AbstractFixedSizeRingBufferTest {


	public static void test(FixedMessageSizeRingBuffer rb) {
        assertEquals(0, rb.size());
		assertTrue(rb.isEmpty());
		assertEquals(EOF, rb.readAcquire());
		
		long writeOffset = rb.writeAcquire();
		assertNotEquals(EOF, writeOffset);
		long fieldOffset = writeOffset+MESSAGE_INDICATOR_SIZE;
		UNSAFE.putInt(fieldOffset,1);
		UNSAFE.putLong(fieldOffset+4,1);
		// blah blah, not writing the rest
		
		rb.writeRelease(writeOffset);
		assertEquals(1, rb.size());
		assertTrue(!rb.isEmpty());
		long readOffset = rb.readAcquire();
		fieldOffset = readOffset + MESSAGE_INDICATOR_SIZE;
		assertNotEquals(EOF, readOffset);
		assertEquals(writeOffset, readOffset);
		assertEquals(1, UNSAFE.getInt(fieldOffset));
		assertEquals(1L, UNSAFE.getLong(fieldOffset+4));
		rb.readRelease(readOffset);
		
		assertEquals(0, rb.size());
		assertTrue(rb.isEmpty());
		assertEquals(EOF, rb.readAcquire());
    }

}
