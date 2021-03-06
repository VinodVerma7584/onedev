package io.onedev.server.web.page.project;

import java.util.Collection;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class NewProjectPage extends LayoutPage {

	public NewProjectPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = new Project();
		
		Collection<String> properties = Sets.newHashSet("name", "description");
		ProjectOwnerBean ownerBean = new ProjectOwnerBean();
		ownerBean.setOwner(SecurityUtils.getUser().getName());
		
		BeanEditor editor = BeanContext.edit("editor", project, properties, false);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				ProjectManager projectManager = OneDev.getInstance(ProjectManager.class);
				Project projectWithSameName = projectManager.find(project.getName());
				if (projectWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another project");
				} else {
					project.setOwner(OneDev.getInstance(UserManager.class).findByName(ownerBean.getOwner()));
					projectManager.create(project);
					Session.get().success("New project created");
					setResponsePage(ProjectBlobPage.class, ProjectBlobPage.paramsOf(project));
				}
			}
			
		};
		form.add(editor);
		form.add(BeanContext.edit("ownerEditor", ownerBean).setVisible(SecurityUtils.isAdministrator()));
		
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProjectResourceReference()));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canCreateProjects();
	}
	
}
