package org.intermine.web.struts;

/*
 * Copyright (C) 2002-2010 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.intermine.api.InterMineAPI;
import org.intermine.api.profile.Profile;
import org.intermine.api.search.Scope;
import org.intermine.api.tag.TagNames;
import org.intermine.api.template.TemplateManager;
import org.intermine.api.template.TemplateQuery;
import org.intermine.api.tracker.TemplateTracker;
import org.intermine.api.tracker.TrackerDelegate;
import org.intermine.web.logic.aspects.Aspect;
import org.intermine.web.logic.session.SessionMethods;

/**
 * Display the query builder (if there is a curernt query) or redirect to project.sitePrefix.
 *
 * @author Tom Riley
 */
public class BeginAction extends InterMineAction
{
    private static final Logger LOG = Logger.getLogger(TemplateTracker.class);
    private static final Integer MAX_TEMPLATES = new Integer(10);

     /**
     * Either display the query builder or redirect to project.sitePrefix.
     *
     * @param mapping
     *            The ActionMapping used to select this instance
     * @param form
     *            The optional ActionForm bean for this request (if any)
     * @param request
     *            The HTTP request we are processing
     * @param response
     *            The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception
     *                if the application business logic throws an exception
     */
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
        throws Exception {

        HttpSession session = request.getSession();
        final InterMineAPI im = SessionMethods.getInterMineAPI(session);
        ServletContext servletContext = session.getServletContext();

        Properties properties = SessionMethods.getWebProperties(servletContext);

        // If GALAXY_URL is sent from a Galaxy server, then save it in the session; if not, read
        // the default value from web.properties and save it in the session
        if (request.getParameter("GALAXY_URL") != null) {
            request.getSession().setAttribute("GALAXY_URL",
                    request.getParameter("GALAXY_URL"));
            String msg = properties.getProperty("galaxy.welcomeMessage");
            SessionMethods.recordMessage(msg, session);
        } else {
            request.getSession().setAttribute(
                    "GALAXY_URL",
                    properties.getProperty("galaxy.baseurl.default")
                            + properties.getProperty("galaxy.url.value"));
        }

        /* count number of templates and bags */
        request.setAttribute("bagCount", new Integer(im.getBagManager()
                .getGlobalBags().size()));
        request.setAttribute("templateCount", new Integer(im
                .getTemplateManager().getGlobalTemplates().size()));

        /*most popular template*/
        TrackerDelegate trackerDelegate = im.getTrackerDelegate();
        if (trackerDelegate != null) {
            trackerDelegate.setTemplateManager(im.getTemplateManager());
            String templateName = trackerDelegate.getMostPopularTemplate();
            if (templateName != null) {
                Profile profile = SessionMethods.getProfile(session);
                TemplateQuery template = im.getTemplateManager()
                                         .getTemplate(profile, templateName, Scope.ALL);
                if (template != null) {
                    request.setAttribute("mostPopularTemplate", template.getTitle());
                } else {
                    LOG.error("The most popular template " + templateName + "is not a public");
                }
            }
        }

        List<TemplateQuery> templates = null;
        TemplateManager templateManager = im.getTemplateManager();
        Map<String, Aspect> aspects = SessionMethods.getAspects(servletContext);
        Map<String, List<TemplateQuery>> aspectQueries = new HashMap<String, List<TemplateQuery>>();
        for (String aspect : aspects.keySet()) {
            templates = templateManager.getAspectTemplates(TagNames.IM_ASPECT_PREFIX + aspect,
                    MAX_TEMPLATES);
            aspectQueries.put(aspect, templates);
        }

        request.setAttribute("aspectQueries", aspectQueries);

        String[] beginQueryClasses = (properties.get("begin.query.classes").toString())
            .split("[ ,]+");
        request.setAttribute("beginQueryClasses", beginQueryClasses);

        return mapping.findForward("begin");
    }
}
