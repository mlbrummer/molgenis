package org.molgenis.ui.menumanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.ui.MenuType;
import org.molgenis.ui.Molgenis;
import org.molgenis.ui.PluginType;
import org.molgenis.ui.XmlMolgenisUiLoader;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuItem;
import org.molgenis.ui.menu.MenuItemType;
import org.molgenis.ui.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.GsonBuilder;

public class MenuManagerServiceImpl implements MenuManagerService, ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(MenuManagerServiceImpl.class);

	public static final String KEY_MOLGENIS_MENU = "molgenis.menu";

	private final MenuReaderService menuReaderService;
	private final MolgenisSettings molgenisSettings;
	private final MolgenisPluginRegistry molgenisPluginRegistry;

	@Autowired
	public MenuManagerServiceImpl(MenuReaderService menuReaderService, MolgenisSettings molgenisSettings,
			MolgenisPluginRegistry molgenisPluginRegistry)
	{
		if (menuReaderService == null) throw new IllegalArgumentException("menuReaderService is null");
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		if (molgenisPluginRegistry == null) throw new IllegalArgumentException("molgenisPluginRegistry is null");
		this.menuReaderService = menuReaderService;
		this.molgenisSettings = molgenisSettings;
		this.molgenisPluginRegistry = molgenisPluginRegistry;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_READ_MENUMANAGER')")
	@Transactional(readOnly = true)
	public Menu getMenu()
	{
		return menuReaderService.getMenu();
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_READ_MENUMANAGER')")
	@Transactional(readOnly = true)
	public Iterable<MolgenisPlugin> getPlugins()
	{
		return molgenisPluginRegistry;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SYSTEM, ROLE_SU, ROLE_PLUGIN_WRITE_MENUMANAGER')")
	@Transactional
	public void saveMenu(Menu molgenisMenu)
	{
		String menuJson = new GsonBuilder().create().toJson(molgenisMenu);
		molgenisSettings.setProperty(KEY_MOLGENIS_MENU, menuJson);
	}

	/**
	 * Backwards compatibility: load default menu on application startup if no menu exists
	 * 
	 * @param event
	 */
	@Override
	@RunAsSystem
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		if (!molgenisSettings.propertyExists(KEY_MOLGENIS_MENU))
		{
			Molgenis molgenis;
			try
			{
				molgenis = new XmlMolgenisUiLoader().load();
			}
			catch (IOException e)
			{
				// default menu does not exist, no op
				return;
			}

			LOG.info("Creating default menu from XML");
			loadDefaultMenu(molgenis);
		}
	}

	private void loadDefaultMenu(Molgenis molgenis)
	{
		Menu molgenisMenu = new Menu();
		parseDefaultMenuRec(molgenisMenu, molgenis.getMenu());
		saveMenu(molgenisMenu);
	}

	private void parseDefaultMenuRec(MenuItem menuItem, Object defaultMenuObj)
	{
		if (defaultMenuObj instanceof MenuType)
		{
			MenuType menuType = (MenuType) defaultMenuObj;
			menuItem.setId(menuType.getName());
			menuItem.setLabel(menuType.getLabel());
			menuItem.setType(MenuItemType.MENU);

			List<Object> defaultMenuItems = menuType.getMenuOrPlugin();
			if (defaultMenuItems != null)
			{
				List<MenuItem> items = new ArrayList<MenuItem>(defaultMenuItems.size());
				for (Object defaultSubMenuObj : defaultMenuItems)
				{
					MenuItem subMenuItem = new MenuItem();
					parseDefaultMenuRec(subMenuItem, defaultSubMenuObj);
					items.add(subMenuItem);
				}
				menuItem.setItems(items);
			}
		}
		else if (defaultMenuObj instanceof PluginType)
		{
			PluginType pluginType = (PluginType) defaultMenuObj;
			menuItem.setId(pluginType.getId());
			menuItem.setLabel(pluginType.getName());
			menuItem.setType(MenuItemType.PLUGIN);

			String url = pluginType.getUrl();
			int idx;
			if ((idx = url.indexOf('?')) != -1)
			{
				String params = url.substring(idx + 1);
				menuItem.setParams(params);
			}
		}
		else
		{
			throw new RuntimeException("Unknown menu object class [" + defaultMenuObj.getClass().getName() + "]");
		}
	}
}
