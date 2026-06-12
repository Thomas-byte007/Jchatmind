import React, { useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Card,
  Typography,
  Upload,
  Table,
  Popconfirm,
  Space,
  message,
  Empty,
  Progress,
} from "antd";
import {
  BookOutlined,
  DeleteOutlined,
  FileOutlined,
  InboxOutlined,
} from "@ant-design/icons";
import type { UploadProps } from "antd";
import { useKnowledgeBases } from "../../hooks/useKnowledgeBases.ts";
import { useDocuments } from "../../hooks/useDocuments.ts";
import { uploadDocument, type DocumentVO } from "../../api/api.ts";

const { Title, Text, Paragraph } = Typography;
const { Dragger } = Upload;

/** ?????? */
interface UploadingFile {
  uid: string;
  name: string;
  percent: number;
  status: "uploading" | "success" | "error";
  errorMsg?: string;
}

/** ??????? */
const ACCEPTED_FORMATS = ".md,.txt,.markdown";

const KnowledgeBaseView: React.FC = () => {
  const { knowledgeBaseId } = useParams<{ knowledgeBaseId?: string }>();
  const { knowledgeBases } = useKnowledgeBases();
  const { documents, loading, refreshDocuments, deleteDocument } =
    useDocuments(knowledgeBaseId);

  const [uploadingFiles, setUploadingFiles] = useState<UploadingFile[]>([]);

  // ????????????
  const currentKnowledgeBase = useMemo(() => {
    if (!knowledgeBaseId) return null;
    return (
      knowledgeBases.find((kb) => kb.knowledgeBaseId === knowledgeBaseId) ||
      null
    );
  }, [knowledgeBaseId, knowledgeBases]);

  // ??????(???????)
  const handleUpload: UploadProps["customRequest"] = async (options) => {
    const { file, onSuccess, onError } = options;
    const uid = (file as File).name + Date.now();

    // ???????
    setUploadingFiles((prev) => [
      ...prev,
      { uid, name: (file as File).name, percent: 0, status: "uploading" },
    ]);

    if (!knowledgeBaseId) {
      message.error("???????");
      setUploadingFiles((prev) =>
        prev.map((f) =>
          f.uid === uid ? { ...f, status: "error", errorMsg: "??????" } : f
        )
      );
      onError?.(new Error("??????"));
      return;
    }

    try {
      await uploadDocument(knowledgeBaseId, file as File);
      setUploadingFiles((prev) =>
        prev.map((f) =>
          f.uid === uid ? { ...f, percent: 100, status: "success" } : f
        )
      );
      message.success(`${(file as File).name} ????`);
      await refreshDocuments();
      onSuccess?.(file);
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : "????";
      setUploadingFiles((prev) =>
        prev.map((f) =>
          f.uid === uid ? { ...f, status: "error", errorMsg } : f
        )
      );
      message.error(`${(file as File).name} ????: ${errorMsg}`);
      onError?.(error as Error);
    }
  };

  // ???????
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return "0 B";
    const k = 1024;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
  };

  // ?????
  const columns = [
    {
      title: "???",
      dataIndex: "filename",
      key: "filename",
      render: (text: string) => (
        <Space>
          <FileOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: "??",
      dataIndex: "filetype",
      key: "filetype",
      width: 120,
    },
    {
      title: "??",
      dataIndex: "size",
      key: "size",
      width: 120,
      render: (size: number) => formatFileSize(size),
    },
    {
      title: "??",
      key: "action",
      width: 100,
      render: (_: unknown, record: DocumentVO) => (
        <Popconfirm
          title="???????????"
          description="????????"
          onConfirm={() => deleteDocument(record.id)}
          okText="??"
          cancelText="??"
        >
          <button className="text-red-500 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm">
            ??
          </button>
        </Popconfirm>
      ),
    },
  ];

  // ??????????
  if (!knowledgeBaseId) {
    return (
      <div className="flex flex-col h-full items-center justify-center p-6">
        <Empty
          image={<BookOutlined className="text-6xl text-gray-300" />}
          description={
            <div className="mt-4">
              <Title level={4} type="secondary">
                ??????
              </Title>
              <Text type="secondary" className="text-sm">
                ?????????????????????
              </Text>
            </div>
          }
        />
      </div>
    );
  }

  // ??????
  if (!currentKnowledgeBase) {
    return (
      <div className="flex flex-col h-full items-center justify-center p-6">
        <Empty
          description={
            <div className="mt-4">
              <Title level={4} type="secondary">
                ??????
              </Title>
              <Text type="secondary" className="text-sm">
                ?????? ID ????
              </Text>
            </div>
          }
        />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full p-6 overflow-y-auto">
      <div className="max-w-6xl w-full mx-auto">
        {/* ??????? */}
        <div className="mb-3">
          <Card>
            <div className="flex items-start gap-4">
              <div className="w-16 h-16 rounded-lg bg-gradient-to-br from-blue-200 to-purple-200 dark:from-blue-900 dark:to-purple-900 flex items-center justify-center text-3xl shrink-0">
                <BookOutlined />
              </div>
              <div className="flex-1">
                <Title level={3} className="mb-2">
                  {currentKnowledgeBase.name}
                </Title>
                {currentKnowledgeBase.description && (
                  <Paragraph className="text-gray-600 dark:text-gray-300 mb-0">
                    {currentKnowledgeBase.description}
                  </Paragraph>
                )}
                <Text type="secondary" className="text-sm">
                  ??? ID: {currentKnowledgeBase.knowledgeBaseId}
                </Text>
              </div>
            </div>
          </Card>
        </div>

        {/* ?????? */}
        <div className="mb-3">
          <Card title="????">
            <Dragger
              multiple
              customRequest={handleUpload}
              showUploadList={false}
              accept={ACCEPTED_FORMATS}
            >
              <p className="text-5xl text-blue-400 dark:text-blue-300 mb-4">
                <InboxOutlined />
              </p>
              <p className="text-base font-medium text-gray-700 dark:text-gray-300">
                ?????????????
              </p>
              <p className="text-sm text-gray-400 dark:text-gray-500 mt-2">
                ????: Markdown (.md)???? (.txt)
              </p>
              <p className="text-xs text-gray-400 dark:text-gray-500">
                ?????????
              </p>
            </Dragger>

            {/* ?????? */}
            {uploadingFiles.length > 0 && (
              <div className="mt-4 space-y-2">
                {uploadingFiles.map((file) => (
                  <div
                    key={file.uid}
                    className="flex items-center gap-3 p-2 rounded bg-gray-50 dark:bg-gray-800"
                  >
                    <FileOutlined className="text-gray-500 dark:text-gray-400 shrink-0" />
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <Text className="text-sm truncate" ellipsis>
                          {file.name}
                        </Text>
                        <Text
                          className={`text-xs shrink-0 ml-2 ${
                            file.status === "success"
                              ? "text-green-500"
                              : file.status === "error"
                              ? "text-red-500"
                              : "text-blue-500"
                          }`}
                        >
                          {file.status === "uploading" && "???..."}
                          {file.status === "success" && "??"}
                          {file.status === "error" &&
                            `??: ${file.errorMsg}`}
                        </Text>
                      </div>
                      {file.status === "uploading" && (
                        <Progress percent={file.percent} size="small" />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>

        {/* ???? */}
        <div className="mb-3">
          <Card title={`???? (${documents.length})`}>
            {loading ? (
              <div className="text-center py-8">
                <Text type="secondary">???...</Text>
              </div>
            ) : documents.length === 0 ? (
              <Empty
                description={<Text type="secondary">????,?????</Text>}
              />
            ) : (
              <Table
                columns={columns}
                dataSource={documents}
                rowKey="id"
                pagination={{
                  pageSize: 10,
                  showTotal: (total) => `? ${total} ?`,
                }}
              />
            )}
          </Card>
        </div>
      </div>
    </div>
  );
};

export default KnowledgeBaseView;
