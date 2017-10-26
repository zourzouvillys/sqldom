package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC)
@Value.Modifiable
public interface JoinSource extends SelectSource {

  @Override
  default String exportedName() {
    return source().exportedName();
  }

  SelectSource source();

  SelectSource joinWith();

  JoinType joinType();

  SqlExpr using();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitJoin(this);
  }

}
