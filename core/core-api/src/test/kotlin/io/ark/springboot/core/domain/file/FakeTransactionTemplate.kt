package io.ark.springboot.core.domain.file

import io.mockk.mockk
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate

class FakeTransactionTemplate : TransactionTemplate() {

    override fun <T : Any?> execute(action: TransactionCallback<T>): T? {
        // 테스트에서는 단순히 액션을 실행하고 결과를 반환
        return try {
            action.doInTransaction(mockk<TransactionStatus>())
        } catch (e: Exception) {
            // 예외 발생 시 null 반환
            null
        }
    }
}
