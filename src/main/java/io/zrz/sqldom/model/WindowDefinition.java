package io.zrz.sqldom.model;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface WindowDefinition extends WindowRef {

  @Nullable
  SqlExpr partitionBy();

  @Nullable
  OrderByClause orderBy();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitWindowDefinition(this);
  }

}
