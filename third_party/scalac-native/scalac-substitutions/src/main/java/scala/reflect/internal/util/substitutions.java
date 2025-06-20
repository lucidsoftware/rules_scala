package scala.reflect.internal.util;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MutableCallSite;

/*
 * A very dumb version of {@link AlmostFinalValue} that's compatible with GraalVM. We should
 * probably refactor the usages of {@link AlmostFinalValue} instead because using
 * {@link MethodHandle} will no longer improve performance under GraalVM, but it'll probably
 * optimize it all away anyway.
 */
@Substitute
@TargetClass(className = "scala.reflect.internal.util.AlmostFinalValue")
final class Target_scala_reflect_internal_util_AlmostFinalValue {
  private static final MethodHandle K_FALSE = MethodHandles.constant(boolean.class, false);
  private static final MethodHandle K_TRUE = MethodHandles.constant(boolean.class, true);

  @Substitute
  MethodHandle invoker = new MutableCallSite(K_FALSE).dynamicInvoker();

  @Substitute
  public Target_scala_reflect_internal_util_AlmostFinalValue() {}

  @Substitute
  void toggleOnAndDeoptimize() {
    invoker = new MutableCallSite(K_TRUE).dynamicInvoker();
  }
}
