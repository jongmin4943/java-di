package com.interface21.beans.factory.config;


import com.interface21.beans.BeanDefinitionException;
import com.interface21.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleBeanDefinitionTest {

    @Test
    @DisplayName("beanClassName 은 클래스명의 앞글자를 소문자로만 바꾼 클래스 이름이다.")
    void beanClassNameTest() {
        final SimpleBeanDefinition simpleBeanDefinition = new SimpleBeanDefinition(NoArgConstructorClass.class);

        final String beanClassName = simpleBeanDefinition.getBeanClassName();

        assertThat(beanClassName).isEqualTo("noArgConstructorClass");
    }

    @Test
    @DisplayName("생성자가 존재하지 않으면 기본 생성자를 반환한다.")
    void findBeanConstructorWithNoArgConstructor() {
        final SimpleBeanDefinition beanDefinition = new SimpleBeanDefinition(NoArgConstructorClass.class);

        final Constructor<?> constructor = beanDefinition.getConstructor();

        assertThat(constructor.getParameterCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("생성자가 하나만 존재하면 해당 생성자를 반환한다.")
    void findBeanConstructorWithOneConstructor() {
        final SimpleBeanDefinition beanDefinition = new SimpleBeanDefinition(OneConstructorClass.class);

        final Constructor<?> constructor = beanDefinition.getConstructor();

        assertThat(constructor.getParameterCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Autowired 가 붙은 생성자를 반환한다.")
    void findBeanConstructorWithAutowired() {
        final SimpleBeanDefinition beanDefinition = new SimpleBeanDefinition(AutowiredConstructorClass.class);
        final Constructor<?> constructor = beanDefinition.getConstructor();

        assertThat(constructor.getParameterCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Autowired 없이 생성자가 여러개면 예외를 던진다.")
    void findBeanConstructorWithMultipleConstructors() {
        assertThatThrownBy(() -> new SimpleBeanDefinition(MultipleConstructorsClass.class))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("Class doesn't contain matching constructor for autowiring");
    }

    @Test
    @DisplayName("Autowired 를 가진 생성자가 여러개면 예외를 던진다.")
    void findBeanConstructorWithMultipleAutowired() {
        assertThatThrownBy(() -> new SimpleBeanDefinition(MultipleAutowiredConstructorsClass.class))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("Only one constructor can have @Autowired annotation");
    }

    public static class NoArgConstructorClass {
    }

    public static class OneConstructorClass {
        public OneConstructorClass(final String param) {
        }
    }

    public static class AutowiredConstructorClass {
        public AutowiredConstructorClass() {
        }

        @Autowired
        public AutowiredConstructorClass(final String param) {
        }
    }

    public static class MultipleConstructorsClass {
        public MultipleConstructorsClass() {
        }

        public MultipleConstructorsClass(final String param) {
        }
    }

    public static class MultipleAutowiredConstructorsClass {
        @Autowired
        public MultipleAutowiredConstructorsClass() {
        }

        @Autowired
        public MultipleAutowiredConstructorsClass(final String param) {
        }
    }

}