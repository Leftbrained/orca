/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.pipeline.model;

import java.io.Serializable;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.netflix.spinnaker.orca.ExecutionStatus;
import com.netflix.spinnaker.security.AuthenticatedRequest;
import com.netflix.spinnaker.security.User;
import static com.netflix.spinnaker.orca.ExecutionStatus.NOT_STARTED;
import static com.netflix.spinnaker.orca.pipeline.model.Execution.ExecutionType.ORCHESTRATION;
import static com.netflix.spinnaker.orca.pipeline.model.Execution.ExecutionType.PIPELINE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;

public class Execution implements Serializable {

  public Execution(ExecutionType type, String application) {
    this(type, UUID.randomUUID().toString(), application);
  }

  @JsonCreator
  public Execution(
    @JsonProperty("type") ExecutionType type,
    @JsonProperty("id") String id,
    @JsonProperty("application") String application) {
    this.type = type;
    this.id = id;
    this.application = application;
  }

  private final ExecutionType type;

  public @Nonnull ExecutionType getType() {
    return type;
  }

  private String id;

  public @Nonnull String getId() {
    return id;
  }

  public void setId(@Nonnull String id) {
    this.id = id;
  }

  private String application;

  public @Nonnull String getApplication() {
    return application;
  }

  public void setApplication(@Nonnull String application) {
    this.application = application;
  }

  private String name;

  public @Nullable String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  private Long buildTime;

  public @Nullable Long getBuildTime() {
    return buildTime;
  }

  public void setBuildTime(@Nullable Long buildTime) {
    this.buildTime = buildTime;
  }

  private boolean canceled;

  public boolean isCanceled() {
    return canceled;
  }

  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  private String canceledBy;

  public @Nullable String getCanceledBy() {
    return canceledBy;
  }

  public void setCanceledBy(@Nullable String canceledBy) {
    this.canceledBy = canceledBy;
  }

  private String cancellationReason;

  public @Nullable String getCancellationReason() {
    return cancellationReason;
  }

  public void setCancellationReason(@Nullable String cancellationReason) {
    this.cancellationReason = cancellationReason;
  }

  private boolean limitConcurrent = false;

  public boolean isLimitConcurrent() {
    return limitConcurrent;
  }

  public void setLimitConcurrent(boolean limitConcurrent) {
    this.limitConcurrent = limitConcurrent;
  }

  private boolean keepWaitingPipelines = false;

  public boolean isKeepWaitingPipelines() {
    return keepWaitingPipelines;
  }

  public void setKeepWaitingPipelines(boolean keepWaitingPipelines) {
    this.keepWaitingPipelines = keepWaitingPipelines;
  }

  @Deprecated
  @JsonIgnore
  public @Nonnull Map<String, Object> getContext() {
    return Stage.topologicalSort(stages)
      .map(Stage::getOutputs)
      .map(Map::entrySet)
      .flatMap(Collection::stream)
      .collect(toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (o, o2) -> o2
      ));
  }

  private final List<Stage> stages = new ArrayList<>();

  @JsonManagedReference
  public @Nonnull List<Stage> getStages() {
    return stages;
  }

  private Long startTime;

  public @Nullable Long getStartTime() {
    return startTime;
  }

  public void setStartTime(@Nullable Long startTime) {
    this.startTime = startTime;
  }

  private Long endTime;

  public @Nullable Long getEndTime() {
    return endTime;
  }

  public void setEndTime(@Nullable Long endTime) {
    this.endTime = endTime;
  }

  private ExecutionStatus status = NOT_STARTED;

  public @Nonnull ExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(@Nonnull ExecutionStatus status) {
    this.status = status;
  }

  private AuthenticationDetails authentication;

  public @Nullable AuthenticationDetails getAuthentication() {
    return authentication;
  }

  public void setAuthentication(
    @Nullable AuthenticationDetails authentication) {
    this.authentication = authentication;
  }

  private PausedDetails paused;

  public @Nullable PausedDetails getPaused() {
    return paused;
  }

  public void setPaused(@Nullable PausedDetails paused) {
    this.paused = paused;
  }

  private String origin;

  public @Nullable String getOrigin() {
    return origin;
  }

  public void setOrigin(@Nullable String origin) {
    this.origin = origin;
  }

  private final Map<String, Object> trigger = new HashMap<>();

  public @Nonnull Map<String, Object> getTrigger() {
    return trigger;
  }

  private String description;

  public @Nullable String getDescription() {
    return description;
  }

  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  private String pipelineConfigId;

  public @Nullable String getPipelineConfigId() {
    return pipelineConfigId;
  }

  public void setPipelineConfigId(@Nullable String pipelineConfigId) {
    this.pipelineConfigId = pipelineConfigId;
  }

  private final List<Map<String, Object>> notifications = new ArrayList<>();

  public @Nonnull List<Map<String, Object>> getNotifications() {
    return notifications;
  }

  private final Map<String, Serializable> initialConfig = new HashMap<>();

  public @Nonnull Map<String, Serializable> getInitialConfig() {
    return initialConfig;
  }

  @Nullable
  public Stage namedStage(String type) {
    return stages
      .stream()
      .filter(it -> it.getType().equals(type))
      .findFirst()
      .orElse(null);
  }

  @Nonnull
  public Stage stageById(String stageId) {
    return stages
      .stream()
      .filter(it -> it.getId().equals(stageId))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("No stage with id %s exists", stageId)));
  }

  @Nonnull
  public Stage stageByRef(String refId) {
    return stages
      .stream()
      .filter(it -> it.getRefId().equals(refId))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("No stage with refId %s exists", refId)));
  }

  @Override public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    Execution execution = (Execution) o;

    return id.equals(execution.id);
  }

  @Override public final int hashCode() {
    int result = super.hashCode();
    result = 31 * result + id.hashCode();
    return result;
  }

  public static Execution newOrchestration(String application) {
    return new Execution(ORCHESTRATION, application);
  }

  public static Execution newPipeline(String application) {
    return new Execution(PIPELINE, application);
  }

  public static class AuthenticationDetails implements Serializable {

    private String user;

    public @Nullable String getUser() {
      return user;
    }

    public void setUser(@Nullable String user) {
      this.user = user;
    }

    private Collection<String> allowedAccounts = emptySet();

    public Collection<String> getAllowedAccounts() {
      return ImmutableSet.copyOf(allowedAccounts);
    }

    public void setAllowedAccounts(Collection<String> allowedAccounts) {
      this.allowedAccounts = ImmutableSet.copyOf(allowedAccounts);
    }

    public AuthenticationDetails() {}

    public AuthenticationDetails(String user, String... allowedAccounts) {
      this.user = user;
      this.allowedAccounts = asList(allowedAccounts);
    }

    public static Optional<AuthenticationDetails> build() {
      Optional<String> spinnakerUserOptional = AuthenticatedRequest.getSpinnakerUser();
      Optional<String> spinnakerAccountsOptional = AuthenticatedRequest.getSpinnakerAccounts();
      if (spinnakerUserOptional.isPresent() || spinnakerAccountsOptional.isPresent()) {
        return Optional.of(new AuthenticationDetails(
          spinnakerUserOptional.orElse(null),
          spinnakerAccountsOptional.map(s -> s.split(",")).orElse(new String[0])
        ));
      }

      return Optional.empty();
    }

    public Optional<User> toKorkUser() {
      return Optional
        .ofNullable(user)
        .map(it -> {
          User user = new User();
          user.setEmail(it);
          user.setAllowedAccounts(allowedAccounts);
          return user;
        });
    }
  }

  public static class PausedDetails implements Serializable {
    String pausedBy;

    public @Nullable String getPausedBy() {
      return pausedBy;
    }

    public void setPausedBy(@Nullable String pausedBy) {
      this.pausedBy = pausedBy;
    }

    String resumedBy;

    public @Nullable String getResumedBy() {
      return resumedBy;
    }

    public void setResumedBy(@Nullable String resumedBy) {
      this.resumedBy = resumedBy;
    }

    Long pauseTime;

    public @Nullable Long getPauseTime() {
      return pauseTime;
    }

    public void setPauseTime(@Nullable Long pauseTime) {
      this.pauseTime = pauseTime;
    }

    Long resumeTime;

    public @Nullable Long getResumeTime() {
      return resumeTime;
    }

    public void setResumeTime(@Nullable Long resumeTime) {
      this.resumeTime = resumeTime;
    }

    @JsonIgnore
    public boolean isPaused() {
      return pauseTime != null && resumeTime == null;
    }

    @JsonIgnore
    public long getPausedMs() {
      return (pauseTime != null && resumeTime != null) ? resumeTime - pauseTime : 0;
    }
  }

  public enum ExecutionType {
    PIPELINE, ORCHESTRATION;

    @Override public String toString() {
      return name().toLowerCase();
    }
  }
}
