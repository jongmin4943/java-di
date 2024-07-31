package com.interface21.beans.factory.support;

import com.interface21.beans.BeanCurrentlyInCreationException;
import com.interface21.beans.BeanInstantiationException;
import com.interface21.beans.NoSuchBeanDefinitionException;
import com.interface21.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.JdbcSampleRepository;
import samples.SampleController;
import samples.SampleControllerInterface;
import samples.SampleService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultListableBeanFactoryTest {

    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        final BeanScanner beanScanner = new BeanScanner("samples");
        this.beanFactory = new DefaultListableBeanFactory(beanScanner.scan());
        beanFactory.initialize();
    }

    @Test
    public void di() {
        final var sampleController = beanFactory.getBean(SampleController.class);

        assertNotNull(sampleController);
        assertNotNull(sampleController.getSampleService());

        final var sampleService = sampleController.getSampleService();
        assertNotNull(sampleService.getSampleRepository());
    }

    @Test
    @DisplayName("인터페이스 타입으로도 등록된 구현체 bean 을 찾을 수 있다.")
    public void getBeanTest() {
        final var sampleController = beanFactory.getBean(SampleControllerInterface.class);

        assertThat(sampleController).isInstanceOf(SampleController.class);
    }

    @Test
    @DisplayName("등록되지 않은 타입으로 bean 을 찾을 경우 예외를 던진다.")
    public void getNoSuchBeanTest() {
        assertThatThrownBy(() -> beanFactory.getBean(NoBeanClass.class))
                .isInstanceOf(NoSuchBeanDefinitionException.class);
    }

    private static class NoBeanClass {
    }

    @Test
    @DisplayName("bean 들의 모든 class 정보를 가져올 수 있다.")
    void getBeanClassesTest() {
        final Set<Class<?>> beanClasses = beanFactory.getBeanClasses();

        assertThat(beanClasses).containsExactlyInAnyOrder(SampleController.class, SampleService.class, JdbcSampleRepository.class);
    }

    @Test
    @DisplayName("clear 시 모든 정보가 지워진다.")
    void clearTest() {
        beanFactory.clear();

        assertSoftly(softly -> {
            softly.assertThat(beanFactory.getBeanClasses()).isEmpty();
            softly.assertThatThrownBy(() -> beanFactory.getBean(SampleController.class))
                    .isInstanceOf(NoSuchBeanDefinitionException.class);
        });
    }

    @Test
    @DisplayName("Bean 생성 시 생성자에서 예외가 던져지면 실패한다.")
    void initializeFailWithExceptionTest() {
        final SimpleBeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry(Set.of(ConstructorExceptionClass.class));
        final DefaultListableBeanFactory wrongBeanFactory = new DefaultListableBeanFactory(beanDefinitionRegistry);

        assertThatThrownBy(wrongBeanFactory::initialize)
                .isInstanceOf(BeanInstantiationException.class)
                .rootCause()
                .isInstanceOf(IllegalStateException.class);
    }

    private static class ConstructorExceptionClass {
        @Autowired
        public ConstructorExceptionClass() {
            throw new IllegalStateException();
        }
    }

    @Test
    @DisplayName("구체 클래스가 아닌 클래스로 Bean 생성 시 예외가 던져지면 실패한다.")
    void initializeFailWithNotConcreteClassTest() {
        final SimpleBeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry(Set.of(AbstractClass.class));
        final DefaultListableBeanFactory wrongBeanFactory = new DefaultListableBeanFactory(beanDefinitionRegistry);

        assertThatThrownBy(wrongBeanFactory::initialize)
                .isInstanceOf(BeanInstantiationException.class)
                .rootCause()
                .isInstanceOf(InstantiationException.class);
    }

    private static abstract class AbstractClass {
        @Autowired
        public AbstractClass() {
        }
    }

    @Test
    @DisplayName("Bean 생성 시 생성자가 private 이면 실패한다.")
    void initializeFailWithPrivateTest() {
        final SimpleBeanDefinitionRegistry beanDefinitionRegistry = new SimpleBeanDefinitionRegistry(Set.of(PrivateConstructorClass.class));
        final DefaultListableBeanFactory wrongBeanFactory = new DefaultListableBeanFactory(beanDefinitionRegistry);

        assertThatThrownBy(wrongBeanFactory::initialize)
                .isInstanceOf(BeanInstantiationException.class)
                .rootCause()
                .isInstanceOf(IllegalAccessException.class);
    }

    private static class PrivateConstructorClass {
        @Autowired
        private PrivateConstructorClass() {
        }
    }

    @Test
    void name() {
        final BeanScanner circularBeanScanner = new BeanScanner("circular.samples");
        final DefaultListableBeanFactory circularBeanFactory = new DefaultListableBeanFactory(circularBeanScanner.scan());

        assertThatThrownBy(circularBeanFactory::initialize)
                .isInstanceOf(BeanCurrentlyInCreationException.class)
                .hasMessageContaining("Circular dependency detected");
    }
}
