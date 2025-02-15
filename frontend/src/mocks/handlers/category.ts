import { rest } from 'msw';
import { categoryURL } from 'constants/apis/url';
import { categories, writingsInCategory, writingsInCategory2 } from 'mocks/categoryContentsMock';
import { AddCategoriesRequest, PatchCategory } from 'types/apis/category';

export const categoryHandlers = [
  // 카테고리 목록 조회
  rest.get(categoryURL, (_, res, ctx) => {
    return res(ctx.json(categories), ctx.delay(300), ctx.status(200));
  }),

  // 카테고리 추가
  rest.post(categoryURL, (req, res, ctx) => {
    const body = req.body as AddCategoriesRequest;

    if (!body || !body.categoryName)
      return res(
        ctx.delay(300),
        ctx.status(404),
        ctx.json({ message: '카테고리 이름은 공백이 될 수 없습니다.' }),
      );

    return res(ctx.delay(300), ctx.set('Location', `/categories/200`), ctx.status(201));
  }),

  // 카테고리 글 목록 조회
  rest.get(`${categoryURL}/:categoryId`, (req, res, ctx) => {
    const categoryId = Number(req.params.categoryId);

    if (categoryId !== 1 && categoryId !== 3)
      return res(
        ctx.delay(300),
        ctx.status(500),
        ctx.json({
          message: '서버에서 예기치 못한 에러가 발생했습니다. 잠시 후 다시 시도해주세요.',
        }),
      );

    if (categoryId === 1) return res(ctx.json(writingsInCategory), ctx.delay(300), ctx.status(200));

    return res(ctx.json(writingsInCategory2), ctx.delay(300), ctx.status(200));
  }),

  // 카테고리 이름 수정
  rest.patch(`${categoryURL}/:categoryId`, (req, res, ctx) => {
    const categoryName = req.body as PatchCategory;

    if (!categoryName)
      return res(
        ctx.delay(300),
        ctx.status(404),
        ctx.json({
          message: '카테고리 이름 수정 에러',
        }),
      );

    return res(ctx.delay(300), ctx.status(204));
  }),

  // 카테고리 경로 수정
  rest.patch(`${categoryURL}/:categoryId`, (req, res, ctx) => {
    const nextCategoryId = req.body as PatchCategory;

    if (!nextCategoryId)
      return res(
        ctx.delay(300),
        ctx.status(404),
        ctx.json({
          message: '카테고리 경로 수정 에러',
        }),
      );

    return res(ctx.delay(300), ctx.status(204));
  }),

  // 카테고리 삭제
  rest.delete(`${categoryURL}/:categoryId`, (_, res, ctx) => {
    return res(ctx.delay(300), ctx.status(204));
  }),
];
