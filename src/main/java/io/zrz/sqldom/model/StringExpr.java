package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface StringExpr extends SqlExpr {

  String value();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitStringExpr(this);
  }

}
