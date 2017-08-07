package org.molgenis.security.twofactor;

import org.molgenis.auth.User;
import org.molgenis.auth.UserMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.QueryImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static com.google.api.client.util.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.populate.IdGenerator.Strategy.SECURE_RANDOM;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.twofactor.RecoveryCodeMetadata.*;

public class RecoveryServiceImpl implements RecoveryService
{
	private static final int RECOVERY_CODE_COUNT = 10;

	private final DataService dataService;
	private final RecoveryCodeFactory recoveryCodeFactory;
	private final IdGenerator idGenerator;

	public RecoveryServiceImpl(DataService dataService, RecoveryCodeFactory recoveryCodeFactory,
			IdGenerator idGenerator)
	{
		this.dataService = requireNonNull(dataService);
		this.recoveryCodeFactory = requireNonNull(recoveryCodeFactory);
		this.idGenerator = requireNonNull(idGenerator);
	}

	@Override
	@Transactional
	public Stream<RecoveryCode> generateRecoveryCodes()
	{
		String userId = getUser().getId();
		deleteOldRecoveryCodes(userId);
		List<RecoveryCode> newRecoveryCodes = generateRecoveryCodes(userId);
		dataService.add(RECOVERY_CODE, newRecoveryCodes.stream());
		return newRecoveryCodes.stream();
	}

	@Override
	@Transactional
	public void useRecoveryCode(String recoveryCode)
	{
		String userId = getUser().getId();
		RecoveryCode existingCode = runAsSystem(() -> dataService.findOne(RECOVERY_CODE,
				new QueryImpl<RecoveryCode>().eq(USER_ID, userId).and().eq(CODE, recoveryCode), RecoveryCode.class));

		if (existingCode != null)
		{
			runAsSystem(() -> dataService.delete(RECOVERY_CODE, existingCode));
		}
		else
		{
			throw new BadCredentialsException("Invalid recovery code");
		}
	}

	@Override
	public Stream<RecoveryCode> getRecoveryCodes()
	{
		String userId = getUser().getId();
		return dataService.findAll(RECOVERY_CODE, new QueryImpl<RecoveryCode>().eq(USER_ID, userId),
				RecoveryCode.class);
	}

	private void deleteOldRecoveryCodes(String userId)
	{
		runAsSystem(() -> {
			Stream<RecoveryCode> recoveryCodes = dataService.findAll(RECOVERY_CODE,
					new QueryImpl<RecoveryCode>().eq(USER_ID, userId), RecoveryCode.class);
			dataService.delete(RECOVERY_CODE, recoveryCodes);
		});
	}

	private List<RecoveryCode> generateRecoveryCodes(String userId)
	{
		List<RecoveryCode> recoveryCodes = newArrayList();
		for (int i = 0; i < RECOVERY_CODE_COUNT; i++)
		{
			RecoveryCode recoveryCode = recoveryCodeFactory.create();
			recoveryCode.setUserId(userId);
			recoveryCode.setCode(idGenerator.generateId(SECURE_RANDOM));
			recoveryCodes.add(recoveryCode);
		}
		return recoveryCodes;
	}

	//FIXME use userservice
	private User getUser()
	{
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = runAsSystem(() -> dataService.findOne(USER,
				new QueryImpl<User>().eq(UserMetaData.USERNAME, userDetails.getUsername()), User.class));

		if (user != null)
		{
			return user;
		}
		else
		{
			throw new UsernameNotFoundException("Can't find user: [" + userDetails.getUsername() + "]");
		}
	}
}