package io.zrz.sqldom.model;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface ParamExpr extends SqlExpr {

  @Nullable
  String name();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitParamExpr(this);
  }

}
