package org.molgenis.metadata.manager.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.metadata.manager.model.*;
import org.molgenis.metadata.manager.service.MetadataManagerService;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { MetadataManagerControllerTest.Config.class, GsonConfig.class })
public class MetadataManagerControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private MenuReaderService menuReaderService;

	@Autowired
	private LanguageService languageService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private MetadataManagerService metadataManagerService;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeMethod()
	{
		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver();
		freeMarkerViewResolver.setSuffix(".ftl");

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(MetadataManagerController.METADATA_MANAGER)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);

		when(languageService.getCurrentUserLanguageCode()).thenReturn("en");
		when(appSettings.getLanguageCode()).thenReturn("nl");

		MetadataManagerController metadataEditorController = new MetadataManagerController(menuReaderService,
				languageService, appSettings, metadataManagerService);

		mockMvc = MockMvcBuilders.standaloneSetup(metadataEditorController)
								 .setMessageConverters(new FormHttpMessageConverter(), gsonHttpMessageConverter)
								 .build();
	}

	@Test
	public void testInit() throws Exception
	{
		mockMvc.perform(get("/plugin/metadata-manager"))
			   .andExpect(status().isOk())
			   .andExpect(view().name("view-metadata-manager"))
			   .andExpect(model().attribute("baseUrl", "/test/path"))
			   .andExpect(model().attribute("lng", "en"))
			   .andExpect(model().attribute("fallbackLng", "nl"));
	}

	@Test
	public void testGetEditorPackages() throws Exception
	{
		when(metadataManagerService.getEditorPackages()).thenReturn(getEditorPackageResponse());
		mockMvc.perform(get("/plugin/metadata-manager/editorPackages"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorPackageResponseJson()));
	}

	@Test
	public void testGetEditorEntityType() throws Exception
	{
		when(metadataManagerService.getEditorEntityType("id_1")).thenReturn(getEditorEntityTypeResponse());
		mockMvc.perform(get("/plugin/metadata-manager/entityType/{id}", "id_1"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testCreateEditorEntityType() throws Exception
	{
		when(metadataManagerService.createEditorEntityType()).thenReturn(getEditorEntityTypeResponse());
		mockMvc.perform(get("/plugin/metadata-manager/create/entityType"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorEntityTypeResponseJson()));
	}

	@Test
	public void testUpsertEntityType() throws Exception
	{
		mockMvc.perform(post("/plugin/metadata-manager/entityType").contentType(APPLICATION_JSON)
																   .content(getEditorEntityTypeJson()))
			   .andExpect(status().isOk());
		verify(metadataManagerService, times(1)).upsertEntityType(getEditorEntityType());
	}

	@Test
	public void testCreateEditorAttribute() throws Exception
	{
		when(metadataManagerService.createEditorAttribute()).thenReturn(getEditorAttributeResponse());
		mockMvc.perform(get("/plugin/metadata-manager/create/attribute"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON))
			   .andExpect(content().string(getEditorAttributeResponseJson()));
	}

	private List<EditorPackageIdentifier> getEditorPackageResponse()
	{
		return newArrayList(EditorPackageIdentifier.create("test", "test"));
	}

	private String getEditorPackageResponseJson()
	{
		return "[{\"id\":\"test\",\"label\":\"test\"}]";
	}

	private EditorEntityTypeResponse getEditorEntityTypeResponse()
	{
		return EditorEntityTypeResponse.create(getEditorEntityType(),
				newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
	}

	private String getEditorEntityTypeResponseJson()
	{
		return "{\"entityType\":" + getEditorEntityTypeJson()
				+ ",\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	private EditorEntityType getEditorEntityType()
	{
		return EditorEntityType.create("id_1", null, ImmutableMap.of(), null, ImmutableMap.of(), false, "backend", null,
				null, ImmutableList.of(), ImmutableList.of(), null, null, ImmutableList.of());
	}

	private String getEditorEntityTypeJson()
	{
		return "{\"id\":\"id_1\",\"labelI18n\":{},\"descriptionI18n\":{},\"abstract0\":false,\"backend\":\"backend\",\"attributes\":[],\"tags\":[],\"lookupAttributes\":[]}";
	}

	private EditorAttributeResponse getEditorAttributeResponse()
	{
		EditorAttribute editorAttribute = EditorAttribute.create("1", null, null, null, null, null, null, null, false,
				false, false, null, ImmutableMap.of(), null, ImmutableMap.of(), false, ImmutableList.of(), null, null,
				false, false, ImmutableList.of(), null, null, null, 1);

		return EditorAttributeResponse.create(editorAttribute,
				newArrayList("en", "nl", "de", "es", "it", "pt", "fr", "xx"));
	}

	private String getEditorAttributeResponseJson()
	{
		return "{\"attribute\":{\"id\":\"1\",\"nullable\":false,\"auto\":false,\"visible\":false,\"labelI18n\":{},\"descriptionI18n\":{},\"aggregatable\":false,\"enumOptions\":[],\"readonly\":false,\"unique\":false,\"tags\":[],\"sequenceNumber\":1},\"languageCodes\":[\"en\",\"nl\",\"de\",\"es\",\"it\",\"pt\",\"fr\",\"xx\"]}";
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MenuReaderService menuReaderService()
		{
			return mock(MenuReaderService.class);
		}

		@Bean
		public LanguageService languageService()
		{
			return mock(LanguageService.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public MetadataManagerService metadataManagerService()
		{
			return mock(MetadataManagerService.class);
		}
	}
}