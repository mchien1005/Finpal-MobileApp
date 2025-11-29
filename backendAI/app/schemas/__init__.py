"""
Pydantic Schemas
"""

from .transaction import (
    TransactionInput,
    CategoryPrediction,
    AnomalyDetectionInput,
    AnomalyDetectionResult
)
from .prediction import (
    SpendingPredictionInput,
    SpendingPredictionResult
)
from .admin import (
    ModelInfo,
    ModelMetrics,
    ModelDetailResponse,
    ModelsListResponse,
    AccuracyDataPoint,
    ModelAccuracyHistory,
    AccuracyHistoryResponse,
    PredictionLogEntry,
    PredictionLogsResponse,
    RetrainRequest,
    RetrainResponse,
    RetrainStatus,
    AdminStatsResponse,
    ModelStatus
)

__all__ = [
    "TransactionInput",
    "CategoryPrediction",
    "AnomalyDetectionInput",
    "AnomalyDetectionResult",
    "SpendingPredictionInput",
    "SpendingPredictionResult",
    # Admin schemas
    "ModelInfo",
    "ModelMetrics",
    "ModelDetailResponse",
    "ModelsListResponse",
    "AccuracyDataPoint",
    "ModelAccuracyHistory",
    "AccuracyHistoryResponse",
    "PredictionLogEntry",
    "PredictionLogsResponse",
    "RetrainRequest",
    "RetrainResponse",
    "RetrainStatus",
    "AdminStatsResponse",
    "ModelStatus"
]
