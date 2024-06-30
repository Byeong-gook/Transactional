package hello.springtx.apply;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Transactional;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired CallService callService;




    @Test
    void printProxy()
    {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void externalCallV2()
    {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig
    {
        @Bean
        CallService callService(){
            return new CallService(internalService());
        }


        @Bean
        InternalService internalService()
        {
            return new InternalService();
        }
    }


    @Slf4j
    @RequiredArgsConstructor
    static class CallService
    {
        @Autowired
        private final InternalService internalService;

        public void external()
        {
            log.info("call external");
            printTxInfo();
            internalService.internal(); // 메서드 내부에서 트랜잭션 메서드 호출


        }

        @Transactional
        public void internal()
        {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo()
        {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }

    }


    static class InternalService
    {
        @Transactional
        public void internal()
        {
            log.info("call internal");
            printTxInfo();

        }


        private void printTxInfo()
        {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);

        }


    }
}

/*
 * public 메서드만 트랜잭션이 적용된다.
 *
 * 클래스 레벨에 트랜잭션을 적용하면
 * 모든 메서드에 트랜잭션이 걸릴 수 있다.
 *  그러면 트랜잭션을 의도하지 않는 곳 까지 트랜잭션이 과도하게 적용된다.
 * 트랜잭션은 주로 비즈니스 로직의 시작점에 걸기 떄문에 대부분 외부에 열어준 곳을 시작점으로 사용한다.
 * 이런 이유로 public 메서드에만 트랜잭션을 설정하도록 한다.
 * 참고로 public이 아닌곳에 @Transaction 어노테이션이 붙어 있으면 예외가 발생하지는 않고, 트랜잭션 적용만 무시된다.
 * */
