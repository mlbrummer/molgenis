package org.molgenis.security.twofactor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { OTPServiceImplTest.Config.class })
public class OTPServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public OTPService otpService()
		{
			return new OTPServiceImpl();
		}
	}

	@Autowired
	private OTPService otpService;

	/**
	 * FIXME(SH): how do we test this properly?
	 */
	@Test(expectedExceptions = BadCredentialsException.class)
	public void tryVerificationKeyTest()
	{
		boolean isValid = otpService.tryVerificationCode("", "secretKey");
		assertEquals(false, isValid);
	}

}