-- =============================================================================
-- SCHEMAS & DATABASE INITIALIZATION: CUSTOMER SERVICE PLATFORM WITH RAG & SENTIMENT
-- Target RDBMS: PostgreSQL 14+ (Highly Recommended) / MySQL 8.0+
-- Author: Senior Database Architect / Capstone Project Schema
-- =============================================================================

-- =============================================================================
-- SECTION 1: POSTGRESQL DDL SCRIPT
-- =============================================================================

-- Enable extension for UUID generation if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -----------------------------------------------------------------------------
-- 1. PHÂN HỆ 1: QUẢN LÝ NGƯỜI DÙNG & PHÂN QUYỀN (USER & AUTH MODULE)
-- -----------------------------------------------------------------------------

DROP TABLE IF EXISTS ai_model_logs CASCADE;
DROP TABLE IF EXISTS bot_feedbacks CASCADE;
DROP TABLE IF EXISTS chat_sources CASCADE;
DROP TABLE IF EXISTS chat_messages CASCADE;
DROP TABLE IF EXISTS chat_sessions CASCADE;
DROP TABLE IF EXISTS document_chunks CASCADE;
DROP TABLE IF EXISTS documents CASCADE;
DROP TABLE IF EXISTS sentiment_results CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    fullname VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'ADMIN', 'AGENT')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Bảng lưu trữ thông tin tài khoản người dùng và phân quyền hệ thống';
COMMENT ON COLUMN users.role IS 'Vai trò: CUSTOMER (Khách hàng), ADMIN (Quản trị viên), AGENT (Tư vấn viên)';

-- -----------------------------------------------------------------------------
-- 2. PHÂN HỆ 2: QUẢN LÝ SẢN PHẨM & DỊCH VỤ (CATALOG MODULE)
-- -----------------------------------------------------------------------------

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

COMMENT ON TABLE categories IS 'Bảng phân loại danh mục sản phẩm/dịch vụ';

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price NUMERIC(15, 2) NOT NULL DEFAULT 0.00 CHECK (price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE products IS 'Bảng thông tin sản phẩm hoặc dịch vụ của doanh nghiệp';

-- -----------------------------------------------------------------------------
-- 3. PHÂN HỆ 3: ĐÁNH GIÁ KHÁCH HÀNG & SENTIMENT ANALYSIS (REVIEW MODULE)
-- -----------------------------------------------------------------------------

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE reviews IS 'Bảng lưu nhận xét và số sao đánh giá của khách hàng';

CREATE TABLE sentiment_results (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL UNIQUE REFERENCES reviews(id) ON DELETE CASCADE,
    label VARCHAR(20) NOT NULL CHECK (label IN ('POSITIVE', 'NEGATIVE', 'NEUTRAL')),
    confidence_score DOUBLE PRECISION NOT NULL CHECK (confidence_score BETWEEN 0.0 AND 1.0),
    model_version VARCHAR(50) NOT NULL,
    analyzed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sentiment_results IS 'Bảng lưu trữ kết quả phân tích cảm xúc do mô hình AI (PhoBERT/DistilBERT) xử lý';

-- -----------------------------------------------------------------------------
-- 4. PHÂN HỆ 4: QUẢN LÝ TRI THỨC RAG (KNOWLEDGE BASE MODULE)
-- -----------------------------------------------------------------------------

CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(10) NOT NULL CHECK (file_type IN ('PDF', 'DOCX', 'TXT')),
    uploaded_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'INDEXED', 'FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE documents IS 'Bảng quản lý file tài liệu tri thức doanh nghiệp tải lên cho RAG';

CREATE TABLE document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    vector_id VARCHAR(100),
    token_count INT CHECK (token_count >= 0),
    CONSTRAINT uq_document_chunk_index UNIQUE (document_id, chunk_index)
);

COMMENT ON TABLE document_chunks IS 'Bảng lưu các đoạn văn bản (chunks) sau khi cắt nhỏ tài liệu gốc';
COMMENT ON COLUMN document_chunks.vector_id IS 'Khóa liên kết tương ứng với Vector ID trong ChromaDB/Qdrant/FAISS';

-- -----------------------------------------------------------------------------
-- 5. PHÂN HỆ 5: RAG CHATBOT & LỊCH SỬ HỘI THOẠI (CHATBOT MODULE)
-- -----------------------------------------------------------------------------

CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL DEFAULT 'Cuộc trò chuyện mới',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE chat_sessions IS 'Bảng quản lý các phiên hội thoại của người dùng với Chatbot';

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    sender VARCHAR(10) NOT NULL CHECK (sender IN ('USER', 'BOT')),
    message_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE chat_messages IS 'Bảng lưu trữ nội dung từng tin nhắn qua lại trong phiên chat';

CREATE TABLE chat_sources (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES chat_messages(id) ON DELETE CASCADE,
    chunk_id BIGINT NOT NULL REFERENCES document_chunks(id) ON DELETE CASCADE,
    relevance_score DOUBLE PRECISION NOT NULL CHECK (relevance_score BETWEEN 0.0 AND 1.0)
);

COMMENT ON TABLE chat_sources IS 'Bảng minh bạch hóa nguồn trích dẫn RAG (xác định câu trả lời lấy từ chunk nào)';

CREATE TABLE bot_feedbacks (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL UNIQUE REFERENCES chat_messages(id) ON DELETE CASCADE,
    is_helpful BOOLEAN NOT NULL,
    user_comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE bot_feedbacks IS 'Bảng ghi nhận phản hồi Thích/Không thích của người dùng cho câu trả lời của Bot';

CREATE TABLE ai_model_logs (
    id BIGSERIAL PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    request_payload JSONB NOT NULL,
    response_payload JSONB NOT NULL,
    latency_ms INT NOT NULL CHECK (latency_ms >= 0),
    logged_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ai_model_logs IS 'Bảng ghi nhận log hoạt động của mô hình AI';

-- -----------------------------------------------------------------------------
-- INDEXES CHO TỐI ƯU HÓA TRUY VẤN (INDEXING STRATEGY)
-- -----------------------------------------------------------------------------

-- Index cho tra cứu tài khoản & phân quyền
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Index cho tìm kiếm sản phẩm theo danh mục
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);

-- Index cho lọc đánh giá và phân tích cảm xúc
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_sentiment_label ON sentiment_results(label);

-- Index cho truy vấn tri thức RAG
CREATE INDEX idx_doc_chunks_document ON document_chunks(document_id);
CREATE INDEX idx_doc_chunks_vector ON document_chunks(vector_id);

-- Index cho truy xuất tin nhắn hội thoại nhanh chóng
CREATE INDEX idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX idx_chat_sources_message ON chat_sources(message_id);
CREATE INDEX idx_chat_sources_chunk ON chat_sources(chunk_id);


-- -----------------------------------------------------------------------------
-- TRIGGER TỰ ĐỘNG CẬP NHẬT updated_at CHO BẢNG users
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();
