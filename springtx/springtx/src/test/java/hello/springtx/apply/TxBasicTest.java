package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 빈의 정의 및 관리
 * 빈의 정의:
 *
 * 빈은 일반적으로 스프링 컨테이너에 의해 생성되고 관리되는 객체입니다.
 * 빈은 보통 스프링 설정 파일 (XML)이나 자바 설정 클래스(@Configuration)에서 정의됩니다.
 *
 *
 * */


/*
*
* @Transactional 애노테이션이 특정 클래스나 메서드에 하나라도 있으면 트랜잭션 AOP는 프록시를 만들어서 스프링 컨테이너에 등록한다.
    그리고 실제 basicService 객체 대신에 프록시인 basicService$
    $CGLIB 를 스프링 빈에 등록한다. 그리고 프록시는 내부에 실제 basicService 를 참조하게 된다. 여기서 핵
    심은 실제 객체 대신에 프록시가 스프링 컨테이너에 등록되었다는 점이다.
    클라이언트인 txBasicTest 는 스프링 컨테이너에 @Autowired BasicService basicService 로 의
    존관계 주입을 요청한다. 스프링 컨테이너에는 실제 객체 대신에 프록시가 스프링 빈으로 등록되어 있기 때문에
    프록시를 주입한다.
    프록시는 BasicService 를 상속해서 만들어지기 때문에 다형성을 활용할 수 있다. 따라서 BasicService
    대신에 프록시인 BasicService$$CGLIB 를 주입할 수 있다.
*
 */
@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired BasicService basicService;
    // Spring이 관리하는 BasicService 빈이 주입됨

    @Test
    void proxyCheck()
    {
        log.info("aop classes={}", basicService.getClass());
        //static import 단축키 alt+enter
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }
    @Test
    void txTest()
    {
            basicService.tx();
            basicService.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig
    {
        @Bean // Spring 컨텍스트에 등록되는 BasicService 빈 생성
        BasicService basicService()
        {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService
    {
        @Transactional
        public void tx()
        {
            log.info("call Tx");
            //현재 쓰레드에 transaction이 적용되어있는지 여부를 확인하는데 사용 isActualTransactionActive();
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }

        public void nonTx()
        {
            log.info("call nonTx");
            //transaction이 적용되어있는지 여부를 확인하는데 사용 isActualTransactionActive();
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
    }
}
