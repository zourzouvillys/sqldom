package io.zrz.sqldom.model;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC)
@Value.Modifiable
public interface SelectField {

  SqlExpr expression();

  @Nullable
  String alias();

}
