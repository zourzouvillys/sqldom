package io.zrz.sqldom.model;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, from = "toImmutable")
@Value.Modifiable
public interface ConflictClause {

  ConflictTarget target();

  @Nullable
  ConflictAction action();

}
