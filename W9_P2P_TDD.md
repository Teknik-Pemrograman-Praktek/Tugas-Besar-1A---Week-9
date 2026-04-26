# Praktikum 9: P2P Lending dengan TDD (Java + JUnit)

## Tujuan

- Menerapkan TDD (Red-Green-Refactor)
- Membuat Unit Test dengan JUnit
- Mengimplementasikan business rule pada studi kasus P2P Lending

Pada praktikum ini, mahasiswa **diberikan satu contoh test case end-to-end** sebagai gambaran alur bisnis P2P Lending.

---

## 1. REQUIREMENT

> Requirement ini merupakan **overview** dari case study. Requirement lebih lengkap akan disampaikan pada dokumen versi berikutnya.

### 1.1 Kebutuhan Aktor

**Borrower**
- Mengajukan pinjaman
- Membayar cicilan

**Lender**
- Mendanai pinjaman

**Sistem (Platform)**
- Menilai kelayakan (credit scoring)
- Mengelola status loan
- Mengelola transaksi

### 1.2 Kebutuhan Fungsional

#### A. Loan Creation
- Borrower harus **terverifikasi (KYC)**
- Loan hanya bisa dibuat jika:
  - `amount > 0`
  - Sistem melakukan **credit scoring**
    - Jika `credit score >= threshold` → status **APPROVED**
    - Jika `credit score < threshold` → status **REJECTED**

#### B. Funding
- Lender dapat mendanai loan
- Lender **tidak boleh** mendanai jika saldo tidak cukup
- Loan menjadi **FUNDED** jika dana terpenuhi

#### C. Disbursement
- Loan hanya bisa di-activate jika `status == FUNDED`
- Setelah di-activate → status menjadi **ACTIVE**

#### D. Repayment
- Borrower dapat membayar loan
- Pembayaran harus `> 0`
- Outstanding balance berkurang
- Jika `outstanding == 0` → status **COMPLETED**

### 1.3 Kebutuhan Non-Fungsional (untuk praktikum)
- Menggunakan Java + Maven
- Menggunakan JUnit untuk testing
- Menggunakan pendekatan **Test-Driven Development (TDD)**
- Setiap behavior diuji dengan unit test

### 1.4 Kebutuhan Domain (Calon Class)

Dari kebutuhan di atas, muncul kandidat class berikut:

- `Borrower`
- `Lender`
- `Loan`
- `LoanService`
- `FundingService`

---

## 2. TAHAPAN SETUP PROJECT

### 2.1 Buat Project Maven

Di VS Code, tekan `Ctrl + Shift + P` → **Java: Create Java Project** → **Maven: Create Maven Project** → pilih `maven-archetype-quickstart`

- Group ID: `com.p2p`
- Artifact ID: `p2p-lending`

### 2.2 Struktur Project

```
p2p-lending/
├── pom.xml
└── src/
    ├── main/java/com/p2p/App.java
    └── test/java/com/p2p/AppTest.java
```

### 2.3 Buat Package

```
com.p2p.domain
com.p2p.service
```

### 2.4 Tambahkan JUnit 5 ke `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.5 Tambahkan Maven Surefire Plugin

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
        </plugin>
    </plugins>
</build>
```

### 2.6 Jalankan Test

```bash
mvn test
```

---

## 3. TAHAPAN IMPLEMENTASI

### 3.1 Pengenalan TDD

Test-Driven Development (TDD) adalah metode pengembangan di mana **test ditulis lebih dulu** sebelum membuat kode program. Siklus prosesnya:

1. **Red** — Tulis test yang gagal
2. **Green** — Buat kode seminimal mungkin agar test lulus
3. **Refactor** — Rapikan struktur kode tanpa mengubah fungsi

Pendekatan ini memastikan kode lebih benar, terstruktur, mudah dipelihara, dan meminimalkan bug sejak awal.

---

### 3.2 Identifikasi Test Case Berdasarkan Functional Requirement

---

## A. LOAN CREATION

### TC-01 — shouldRejectLoanWhenBorrowerNotVerified

**Skenario:**
- Borrower tidak terverifikasi
- Mengajukan loan

**Expected:**
- Exception / loan tidak dibuat

#### RED — Tulis Test Dulu (`LoanServiceTest.java`)

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class LoanServiceTest {

    @Test
    void shouldRejectLoanWhenBorrowerNotVerified() {
        // =====================================================
        // SCENARIO:
        // Borrower tidak terverifikasi (KYC = false)
        // Ketika borrower mengajukan pinjaman
        // Maka sistem harus menolak dengan melempar exception
        // =====================================================

        // Arrange
        Borrower borrower = new Borrower(false, 700); // KYC = false
        LoanService loanService = new LoanService();
        BigDecimal amount = BigDecimal.valueOf(1000);

        // Act
        loanService.createLoan(borrower, amount);

        // Assert
        assertTrue(true);
    }
}
```

#### GREEN — Implementasi

**`Borrower.java`** (`com.p2p.domain`)

```java
package com.p2p.domain;

public class Borrower {

    private boolean verified;
    private int creditScore;

    public Borrower(boolean verified, int creditScore) {
        this.verified = verified;
        this.creditScore = creditScore;
    }

    public boolean isVerified() {
        return verified;
    }

    public int getCreditScore() {
        return creditScore;
    }
}
```

**`Loan.java`** (`com.p2p.domain`)

```java
package com.p2p.domain;

public class Loan {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    private Status status;

    public Loan() {
        this.status = Status.PENDING;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
```

**`LoanService.java`** (`com.p2p.service`)

```java
package com.p2p.service;

import com.p2p.domain.*;
import java.math.BigDecimal;

public class LoanService {

    public Loan createLoan(Borrower borrower, BigDecimal amount) {

        // VALIDASI TC-01: borrower harus sudah KYC
        if (!borrower.isVerified()) {
            throw new IllegalArgumentException("Borrower not verified");
        }

        Loan loan = new Loan();

        // Credit scoring sederhana (sementara)
        if (borrower.getCreditScore() >= 600) {
            loan.setStatus(Loan.Status.APPROVED);
        } else {
            loan.setStatus(Loan.Status.REJECTED);
        }

        return loan;
    }
}
```

#### REFACTOR — Step by Step (Fowler Style)

**Step 1 — Extract Method** (pisahkan validasi)

```java
public Loan createLoan(Borrower borrower, BigDecimal amount) {
    validateBorrower(borrower);
    Loan loan = new Loan();
    loan.setStatus(Loan.Status.APPROVED);
    return loan;
}

private void validateBorrower(Borrower borrower) {
    if (!borrower.isVerified()) {
        throw new IllegalArgumentException("Borrower not verified");
    }
}
```

**Step 2 — Move Method** (pindahkan responsibility ke domain)

```java
// Di LoanService
private void validateBorrower(Borrower borrower) {
    if (!borrower.canApplyLoan()) {
        throw new IllegalArgumentException("Borrower not verified");
    }
}

// Di Borrower.java — tambahkan domain behavior
public boolean canApplyLoan() {
    return verified;
}
```

**Step 3 — Replace Hardcoded Status Logic** (rich domain)

```java
// Di LoanService — gunakan method domain
Loan loan = new Loan();
loan.approve();

// Di Loan.java — tambahkan behavior
public void approve() {
    this.status = Status.APPROVED;
}
```

**Step 4 — Final Clean Service**

```java
public Loan createLoan(Borrower borrower, BigDecimal amount) {
    validateBorrower(borrower);

    Loan loan = new Loan();
    if (borrower.getCreditScore() >= 600) {
        loan.approve();
    } else {
        loan.reject();
    }

    return loan;
}
```

**Step 5 — Final `Loan.java`** (tambahkan domain behavior)

```java
// Domain Behavior
public void approve() {
    this.status = Status.APPROVED;
}

public void reject() {
    this.status = Status.REJECTED;
}
```

**Step 6 — Final `Borrower.java`** (tambahkan domain behavior)

```java
// Domain Behavior
public boolean canApplyLoan() {
    return verified;
}
```

#### Final Result Setelah Refactor

- ✅ `LoanService` hanya melakukan **orchestration** — tidak ada business logic detail
- ✅ `Borrower` punya behavior: `canApplyLoan()`
- ✅ `Loan` punya behavior: `approve()`, `reject()`

**`LoanService.java` — Final**

```java
package com.p2p.service;

import com.p2p.domain.*;
import java.math.BigDecimal;

public class LoanService {

    public Loan createLoan(Borrower borrower, BigDecimal amount) {
        // Validasi (delegasi ke domain)
        validateBorrower(borrower);

        // Buat loan object
        Loan loan = new Loan();

        // Business action (domain behavior)
        if (borrower.getCreditScore() >= 600) {
            loan.approve();
        } else {
            loan.reject();
        }

        return loan;
    }

    private void validateBorrower(Borrower borrower) {
        if (!borrower.canApplyLoan()) {
            throw new IllegalArgumentException("Borrower not verified");
        }
    }
}
```

---

### TC-02 — shouldRejectLoanWhenAmountIsZeroOrNegative

**Skenario:**
- Borrower valid
- `amount <= 0`

**Expected:** Exception

---

### TC-03 — shouldApproveLoanWhenCreditScoreHigh

**Skenario:**
- Borrower verified
- `credit score >= threshold`

**Expected:** `status == APPROVED`

---

### TC-04 — shouldRejectLoanWhenCreditScoreLow

**Skenario:**
- Borrower verified
- `credit score < threshold`

**Expected:** `status == REJECTED`

---

## B. FUNDING

### TC-05 — shouldAllowFundingWhenBalanceSufficient

**Skenario:**
- Lender saldo cukup
- Funding dilakukan

**Expected:** Funding berhasil

---

### TC-06 — shouldRejectFundingWhenBalanceNotEnough

**Skenario:**
- Lender saldo kurang
- Funding dilakukan

**Expected:** Exception

---

### TC-07 — shouldMarkLoanAsFundedWhenFullyFunded

**Skenario:**
- Loan didanai penuh

**Expected:** `status == FUNDED`

---

## C. DISBURSEMENT (ACTIVATION)

### TC-08 — shouldNotActivateLoanIfNotFunded

**Skenario:**
- Loan belum FUNDED

**Expected:** Exception

---

### TC-09 — shouldActivateLoanWhenFunded

**Skenario:**
- `status == FUNDED`
- Loan di-activate

**Expected:** `status == ACTIVE`

---

## D. REPAYMENT

### TC-10 — shouldAllowRepaymentWhenLoanActive

**Skenario:**
- Loan ACTIVE
- Borrower melakukan pembayaran

**Expected:** Pembayaran diterima

---

### TC-11 — shouldRejectRepaymentWhenAmountInvalid

**Skenario:**
- `payment <= 0`

**Expected:** Exception

---

### TC-12 — shouldReduceOutstandingWhenRepay

**Skenario:**
- Loan ACTIVE
- Bayar sebagian

**Expected:** Outstanding berkurang

---

### TC-13 — shouldCompleteLoanWhenOutstandingZero

**Skenario:**
- Loan ACTIVE
- Dibayar lunas (outstanding = 0)

**Expected:** `status == COMPLETED`

---

## Ringkasan Test Cases

| TC | Method Name | Feature | Expected |
|---|---|---|---|
| TC-01 | shouldRejectLoanWhenBorrowerNotVerified | Loan Creation | Exception |
| TC-02 | shouldRejectLoanWhenAmountIsZeroOrNegative | Loan Creation | Exception |
| TC-03 | shouldApproveLoanWhenCreditScoreHigh | Loan Creation | APPROVED |
| TC-04 | shouldRejectLoanWhenCreditScoreLow | Loan Creation | REJECTED |
| TC-05 | shouldAllowFundingWhenBalanceSufficient | Funding | Berhasil |
| TC-06 | shouldRejectFundingWhenBalanceNotEnough | Funding | Exception |
| TC-07 | shouldMarkLoanAsFundedWhenFullyFunded | Funding | FUNDED |
| TC-08 | shouldNotActivateLoanIfNotFunded | Disbursement | Exception |
| TC-09 | shouldActivateLoanWhenFunded | Disbursement | ACTIVE |
| TC-10 | shouldAllowRepaymentWhenLoanActive | Repayment | Berhasil |
| TC-11 | shouldRejectRepaymentWhenAmountInvalid | Repayment | Exception |
| TC-12 | shouldReduceOutstandingWhenRepay | Repayment | Outstanding berkurang |
| TC-13 | shouldCompleteLoanWhenOutstandingZero | Repayment | COMPLETED |
