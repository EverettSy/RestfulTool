/*
  Copyright (C), 2018-2020, ZhangYuanSheng
  FileName: Request
  Author:   ZhangYuanSheng
  Date:     2020/5/2 00:43
  Description: 
  History:
  <author>          <time>          <version>          <desc>
  作者姓名            修改时间           版本号              描述
 */
package com.github.restful.tool.beans;

import com.github.restful.tool.utils.RestUtil;
import com.github.restful.tool.utils.SystemUtil;
import com.github.restful.tool.utils.Icons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ApiService {

    private final NavigatablePsiElement psiElement;
    @Nullable
    private HttpMethod method;
    @Nullable
    private String path;
    @NotNull
    private Icon icon = Icons.getMethodIcon(null);

    private String requestUrl;

    public ApiService(HttpMethod method, @Nullable String path, @Nullable NavigatablePsiElement psiElement) {
        this.setMethod(method);
        if (path != null) {
            this.setPath(path);
        }
        this.psiElement = psiElement;
    }

    public NavigatablePsiElement getPsiElement() {
        return psiElement;
    }

    public void navigate(boolean requestFocus) {
        if (psiElement != null) {
            psiElement.navigate(requestFocus);
        }
    }

    @Nullable
    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(@Nullable HttpMethod method) {
        this.method = method;
        this.icon = Icons.getMethodIcon(method);
    }

    @NotNull
    public Icon getIcon() {
        return icon;
    }

    public Icon getSelectIcon() {
        return Icons.getMethodIcon(this.method, true);
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(@NotNull String path) {
        final String ignore = "//";
        final String prefix = "/";
        path = prefix + path.trim();

        while (path.contains(ignore)) {
            path = path.replace(ignore, prefix);
        }
        this.path = path;
    }

    public void setParent(@NotNull ApiService parent) {
        if ((this.method == null || this.method == HttpMethod.REQUEST) && parent.getMethod() != null) {
            this.setMethod(parent.getMethod());
        }
        String parentPath = parent.getPath();
        if (parentPath != null && parentPath.endsWith("/")) {
            // 去掉末尾的斜杠
            parentPath = parentPath.substring(0, parentPath.length() - 1);
        }
        this.setPath(parentPath + this.path);
    }

    @NotNull
    public ApiService copyWithParent(@Nullable ApiService parent) {
        ApiService apiService = new ApiService(this.method, this.path, this.psiElement);
        if (parent != null) {
            apiService.setParent(parent);
        }
        return apiService;
    }

    @NotNull
    public String getIdentity(String... itemIds) {
        HttpMethod methodTemp = this.method == null ? HttpMethod.REQUEST : this.method;
        String pathTemp = this.path == null ? "" : this.path;

        StringBuilder items = new StringBuilder();
        if (itemIds != null) {
            items.append("-[");
            for (int i = 0; i < itemIds.length; i++) {
                if (i > 0) {
                    items.append(", ");
                }
                items.append(itemIds[i]);
            }
            items.append("]");
        }

        return String.format("{}[%s]%s(%s)%s", methodTemp, pathTemp, icon.getClass(), items.toString());
    }

    public String getRequestUrl() {
        if (requestUrl == null) {
            Project project = this.getPsiElement().getProject();
            GlobalSearchScope scope = this.getPsiElement().getResolveScope();
            requestUrl = SystemUtil.buildUrl(
                    RestUtil.scanListenerProtocol(project, scope),
                    RestUtil.scanListenerPort(project, scope),
                    RestUtil.scanContextPath(project, scope),
                    this.getPath()
            );
        }
        return requestUrl;
    }

    @Override
    public String toString() {
        return this.getIdentity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiService apiService = (ApiService) o;
        if (method != apiService.method) {
            return false;
        }
        return Objects.equals(path, apiService.path);
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}