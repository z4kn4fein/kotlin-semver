<#import "source_set_selector.ftl" as source_set_selector>
<#macro display>
    <nav class="navigation theme-dark" id="navigation-wrapper">
        <@template_cmd name="pathToRoot">
            <a class="library-name--link" href="${pathToRoot}index.html">
                <@template_cmd name="projectName">
                    ${projectName}
                </@template_cmd>
            </a>
        </@template_cmd>
        <button class="navigation-controls--btn navigation-controls--btn_toc ui-kit_mobile-only" id="toc-toggle"
                type="button">Toggle table of contents
        </button>
        <div class="navigation-controls--break ui-kit_mobile-only"></div>
        <div class="library-version" id="library-version">
            <#-- This can be handled by the versioning plugin -->
            <@version/>
        </div>
        <div class="navigation-controls">
            <@source_set_selector.display/>
            <a href="https://github.com/z4kn4fein/kotlin-semver" target="_blank" rel="noopener" class="gh-link">
                <i class="fa fa-github"></i> <span class="repo-name">z4kn4fein/kotlin-semver</span>
            </a>
            <button class="navigation-controls--btn navigation-controls--btn_theme" id="theme-toggle-button"
                    type="button">Switch theme
            </button>
            <div class="navigation-controls--btn navigation-controls--btn_search" id="searchBar" role="button">Search in
                API
            </div>
        </div>
    </nav>
</#macro>