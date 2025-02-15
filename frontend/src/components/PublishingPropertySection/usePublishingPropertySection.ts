import { useMutation } from '@tanstack/react-query';
import { publishWriting as PublishWritingRequest } from 'apis/writings';
import { TabKeys } from 'components/WritingSideBar/WritingSideBar';
import { useToast } from 'hooks/@common/useToast';
import { useState } from 'react';
import { PublishWritingArgs } from 'types/apis/writings';
import { Blog, PublishingPropertyData } from 'types/domain';
import { HttpError } from 'utils/apis/HttpError';

type Args = {
  selectCurrentTab: (tabKey: TabKeys) => void;
};

type PublishWritingToBlogArgs = {
  writingId: number;
  publishTo: Blog;
};

export const usePublishingPropertySection = ({ selectCurrentTab }: Args) => {
  const [propertyFormInfo, setPropertyFormInfo] = useState<PublishingPropertyData>({ tags: [] });
  const toast = useToast();
  const { mutate: publishWriting, isLoading } = useMutation(PublishWritingRequest, {
    onSuccess: () => {
      selectCurrentTab(TabKeys.WritingProperty);
      toast.show({ type: 'success', message: '글 발행에 성공했습니다.' });
    },
    onError: (error) => {
      if (error instanceof HttpError) toast.show({ type: 'error', message: error.message });
    },
  });

  const publishWritingToBlog = ({ writingId, publishTo }: PublishWritingToBlogArgs) => {
    const body = {
      publishTo,
      tags: propertyFormInfo.tags,
    };

    publishWriting({ writingId, body });
  };

  const setTags = (tags: string[]) => {
    setPropertyFormInfo((prev) => ({ ...prev, tags }));
  };

  return { isLoading, setTags, publishWritingToBlog };
};
