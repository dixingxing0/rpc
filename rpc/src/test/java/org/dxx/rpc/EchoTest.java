/**
 * EchoTest.java
 * org.dxx.rpc
 * Copyright (c) 2014, 北京微课创景教育科技有限公司版权所有.
*/

package org.dxx.rpc;

import org.dxx.rpc.share.HelloService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author   dixingxing
 * @Date	 2014-6-25
 */
public class EchoTest {
	static final Logger logger = LoggerFactory.getLogger(EchoTest.class);

	@Test
	public void test() throws Exception {
		RpcUtils.startup();
		Assert.assertEquals("sssssss", RpcUtils.echo(HelloService.class, "sssssss"));
	}

}
