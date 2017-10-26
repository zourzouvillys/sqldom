package io.zrz.sqldom.model;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC)
@Value.Modifiable
public interface ValueListSource extends SelectSource, InsertSource {

  List<SqlExpr[]> rows();

  @Nullable
  String alias();

  @Nullable
  List<String> shape();

  @Override
  default String exportedName() {
    return alias();
  }

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitValueListSelectSource(this);
  }

  @Override
  default <R> R apply(InsertSourceVisitor<R> visitor) {
    return visitor.visitValueListInsertSource(this);
  }

}
